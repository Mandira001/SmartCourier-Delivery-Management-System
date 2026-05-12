import { useEffect, useMemo, useState } from 'react';
import { getMyDeliveries, createDelivery } from '../api.js';
import { motion, AnimatePresence } from 'framer-motion';
import QRCode from 'react-qr-code';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import {
  Truck, MapPin, Search, Send, Box, User, PlusCircle,
  CheckCircle, Clock, AlertTriangle, Package, FileText, X, Weight,
} from 'lucide-react';

export function DeliveriesPage({ token, onMessage }) {
  const [deliveries, setDeliveries]     = useState([]);
  const [loading, setLoading]           = useState(true);
  const [saving, setSaving]             = useState(false);
  const [query, setQuery]               = useState('');
  const [activeFilter, setActiveFilter] = useState('all');
  const [showForm, setShowForm]         = useState(false);
  const [form, setForm] = useState({
    senderName: '', senderPhone: '', senderStreet: '', senderCity: '',
    senderState: '', senderPincode: '', senderCountry: '',
    receiverName: '', receiverPhone: '', receiverStreet: '', receiverCity: '',
    receiverState: '', receiverPincode: '', receiverCountry: '',
    weightKg: '1', distanceKm: '100', deliveryType: 'NATIONAL', serviceType: 'STANDARD',
  });
  const [selectedLabel, setSelectedLabel] = useState(null);
  const [downloading, setDownloading]     = useState(false);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        /*
         * When the page opens, it loads deliveries for the logged-in user.
         * The frontend sends only the JWT. The backend identifies the user email
         * from gateway headers, so the user cannot choose another email from UI.
         */
        const data = await getMyDeliveries(token);
        setDeliveries(data || []);
      } catch (error) {
        onMessage(error.message || 'Failed to load deliveries.');
      } finally {
        setLoading(false);
      }
    };
    if (token) load();
  }, [token]);

  /*
   * Quick-stat counts shown in the stats strip at the top.
   * They are derived from the full delivery list so they always reflect the
   * actual data, regardless of what search text or filter tab is active.
   */
  const quickStats = useMemo(() => {
    const total     = deliveries.length;
    const delivered = deliveries.filter((d) => d.status?.toLowerCase().includes('delivered')).length;
    const active    = deliveries.filter((d) =>
      d.status?.toLowerCase().includes('transit') ||
      d.status?.toLowerCase().includes('pending') ||
      d.status?.toLowerCase().includes('picked') ||
      d.status?.toLowerCase().includes('out')
    ).length;
    const exceptions = deliveries.filter((d) =>
      d.status?.toLowerCase().includes('exception') ||
      d.status?.toLowerCase().includes('failed')
    ).length;
    return { total, delivered, active, exceptions };
  }, [deliveries]);

  const filteredDeliveries = useMemo(() => {
    /*
     * The list can be searched by tracking number, status, sender/receiver name,
     * or city. The activeFilter tab further narrows by status group.
     * useMemo is used because filtering should only run again when the
     * delivery list or search text changes, not on every unrelated render.
     */
    const normalized = query.trim().toLowerCase();

    return deliveries.filter((item) => {
      // Text search
      const textMatch = normalized
        ? [item.id, item.trackingNumber, item.status,
           item.senderAddress?.name, item.receiverAddress?.name,
           item.receiverAddress?.city, item.senderAddress?.city]
            .filter(Boolean).join(' ').toLowerCase().includes(normalized)
        : true;

      // Tab filter
      const s = (item.status || '').toLowerCase();
      const tabMatch =
        activeFilter === 'all'       ? true :
        activeFilter === 'active'    ? (s.includes('transit') || s.includes('pending') || s.includes('picked') || s.includes('out')) :
        activeFilter === 'delivered' ? s.includes('delivered') :
        activeFilter === 'exception' ? (s.includes('exception') || s.includes('failed')) :
        true;

      return textMatch && tabMatch;
    });
  }, [deliveries, query, activeFilter]);

  const handleFieldChange = (field) => (event) =>
    setForm((prev) => ({ ...prev, [field]: event.target.value }));

  /*
   * Live price estimate shown in the form before submission.
   * Mirrors the calculatePrice() method in DeliveryService.java.
   */
  const estimatedPrice = useMemo(() => {
    const w = parseFloat(form.weightKg)   || 1;
    const d = parseFloat(form.distanceKm) || 100;
    const isIntl    = form.deliveryType === 'INTERNATIONAL';
    const isExpress = form.serviceType   === 'EXPRESS';
    const base    = isIntl ? 1500 : 100;
    const rateKg  = isIntl ? (isExpress ? 300 : 200) : (isExpress ? 80 : 50);
    const rateKm  = isIntl ? (isExpress ? 1.5 : 1.0) : (isExpress ? 3.0 : 2.0);
    return Math.round((base + w * rateKg + d * rateKm) * 100) / 100;
  }, [form.weightKg, form.distanceKm, form.deliveryType, form.serviceType]);

  const handleSubmit = async (event) => {
    /*
     * This form creates a new shipment.
     * event.preventDefault() keeps the app from refreshing, and the shipment is
     * submitted through the API layer with the logged-in user's JWT token.
     */
    event.preventDefault();
    setSaving(true);
    /*
     * The form UI stores fields separately for easy input binding:
     * senderName, senderPhone, receiverName, receiverCity, etc.
     *
     * Delivery-service expects a nested JSON structure with senderAddress and
     * receiverAddress. This payload conversion connects the frontend form shape
     * to the backend DTO shape.
     */
    const payload = {
      senderAddress: {
        name: form.senderName, phone: form.senderPhone, street: form.senderStreet,
        city: form.senderCity, state: form.senderState, pincode: form.senderPincode,
        country: form.senderCountry,
      },
      receiverAddress: {
        name: form.receiverName, phone: form.receiverPhone, street: form.receiverStreet,
        city: form.receiverCity, state: form.receiverState, pincode: form.receiverPincode,
        country: form.receiverCountry,
      },
      weightKg:     parseFloat(form.weightKg)   || 1,
      distanceKm:   parseFloat(form.distanceKm) || 100,
      deliveryType: form.deliveryType || 'NATIONAL',
      serviceType:  form.serviceType  || 'STANDARD',
    };

    try {
      /*
       * createDelivery() sends the payload to delivery-service.
       * Backend creates the delivery, generates a unique tracking number,
       * stores it in PostgreSQL, and sends the initial BOOKED event to RabbitMQ.
       */
      const created = await createDelivery(payload, token);
      onMessage('Delivery created successfully.');

      /*
       * After a booking is created, the UI also creates a notification.
       * This block represents a booking confirmation email in the demo flow.
       */
      import('../utils/notifications.js').then(({ notifyUser }) => {
        /*
         * This simulates an email booking receipt.
         * It does not call a real email service. The notification is stored in
         * localStorage so the demo can show an inbox-like experience.
         */
        notifyUser({
          type: 'email',
          title: 'Booking Confirmed: ' + (created.trackingNumber || 'Pending'),
          message: `Your package to ${form.receiverName} in ${form.receiverCity} has been successfully booked. Our delivery partner will pick it up shortly.`,
          trackingNumber: created.trackingNumber,
        });
      });

      /*
       * After backend confirms creation, the new delivery is inserted at the
       * top of the current list. This avoids forcing a full reload of the page
       * just to show the newly created shipment.
       */
      setDeliveries((prev) => [created, ...prev]);
      setShowForm(false);
      setForm({
        senderName: '', senderPhone: '', senderStreet: '', senderCity: '',
        senderState: '', senderPincode: '', senderCountry: '',
        receiverName: '', receiverPhone: '', receiverStreet: '', receiverCity: '',
        receiverState: '', receiverPincode: '', receiverCountry: '',
        weightKg: '1', distanceKm: '100', deliveryType: 'NATIONAL', serviceType: 'STANDARD',
      });
    } catch (error) {
      onMessage(error.message || 'Failed to create delivery.');
    } finally {
      setSaving(false);
    }
  };

  const getStatusBadge = (status) => {
    /*
     * Backend status values are plain strings like BOOKED, IN_TRANSIT, DELIVERED.
     * This helper converts those strings into colored badges so users can scan
     * delivery states quickly without reading every status carefully.
     */
    const s = (status || '').toLowerCase();
    if (s.includes('delivered'))
      return <span className="badge delivered"><CheckCircle size={14}/> Delivered</span>;
    if (s.includes('pending') || s.includes('transit'))
      return <span className="badge pending"><Clock size={14}/> {status}</span>;
    if (s.includes('exception') || s.includes('failed'))
      return <span className="badge" style={{ background: 'rgba(239,68,68,0.12)', color: '#ef4444', border: '1px solid rgba(239,68,68,0.3)' }}>
        <AlertTriangle size={14}/> Exception
      </span>;
    return <span className="badge transit">{status}</span>;
  };

  const downloadLabel = async () => {
    if (!selectedLabel) return;
    setDownloading(true);
    try {
      /*
       * The shipping label is first rendered as normal HTML in the modal.
       * html2canvas captures that DOM element and converts it into an image.
       * This is useful because jsPDF works well with image data.
       */
      const element = document.getElementById('shipping-label');
      const canvas  = await html2canvas(element, { scale: 2 });
      const imgData = canvas.toDataURL('image/png');
      /*
       * jsPDF creates an A4 PDF and places the captured label image inside it.
       * The user can then download a printable shipping label containing
       * sender/receiver details and QR tracking code.
       */
      const pdf       = new jsPDF('p', 'mm', 'a4');
      const pdfWidth  = pdf.internal.pageSize.getWidth();
      const margin    = 15;
      const printWidth  = pdfWidth - margin * 2;
      const printHeight = (canvas.height * printWidth) / canvas.width;
      /*
       * The PDF uses a small margin so the generated label does not touch the
       * paper edges when printed.
       */
      pdf.addImage(imgData, 'PNG', margin, margin, printWidth, printHeight);
      pdf.save(`Shipping-Label-${selectedLabel.trackingNumber}.pdf`);
      onMessage('Shipping label downloaded successfully!');
    } catch {
      onMessage('Failed to generate PDF.');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="w-12 h-12 border-4 border-t-transparent rounded-full animate-spin"
             style={{ borderColor: 'var(--accent-primary)', borderTopColor: 'transparent' }} />
      </div>
    );
  }

  /* ── Filter tab definitions ────────────────────────────────────────── */
  const TABS = [
    { key: 'all',       label: 'All',        count: quickStats.total      },
    { key: 'active',    label: 'Active',     count: quickStats.active     },
    { key: 'delivered', label: 'Delivered',  count: quickStats.delivered  },
    { key: 'exception', label: 'Exceptions', count: quickStats.exceptions },
  ];

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="fade-in">

      {/* ── Page header ──────────────────────────────────────── */}
      <div className="page-header">
        <div>
          <h1>My Deliveries</h1>
          <p>Manage your shipments and book new deliveries.</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
          <PlusCircle size={20} /> {showForm ? 'Cancel' : 'New Shipment'}
        </button>
      </div>

      {/* ── Stats Strip ──────────────────────────────────────── */}
      <div className="stats-strip">
        <div className="stats-strip-card">
          <div className="stats-strip-icon blue"><Package size={20}/></div>
          <div>
            <div className="stats-strip-value">{quickStats.total}</div>
            <div className="stats-strip-label">Total</div>
          </div>
        </div>
        <div className="stats-strip-card">
          <div className="stats-strip-icon amber"><Truck size={20}/></div>
          <div>
            <div className="stats-strip-value">{quickStats.active}</div>
            <div className="stats-strip-label">Active</div>
          </div>
        </div>
        <div className="stats-strip-card">
          <div className="stats-strip-icon green"><CheckCircle size={20}/></div>
          <div>
            <div className="stats-strip-value">{quickStats.delivered}</div>
            <div className="stats-strip-label">Delivered</div>
          </div>
        </div>
        <div className="stats-strip-card">
          <div className="stats-strip-icon red"><AlertTriangle size={20}/></div>
          <div>
            <div className="stats-strip-value">{quickStats.exceptions}</div>
            <div className="stats-strip-label">Exceptions</div>
          </div>
        </div>
      </div>

      {/* ── New Shipment Form ─────────────────────────────────── */}
      <AnimatePresence>
        {showForm && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="overflow-hidden mb-8"
          >
            <div className="glass-card">
              <h2 className="text-xl font-semibold mb-6 flex items-center gap-2">
                <Box className="text-blue-500"/> Booking Details
              </h2>
              <form onSubmit={handleSubmit} className="flex flex-col gap-8">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  {/* Sender */}
                  <div className="space-y-4 p-5 rounded-xl" style={{ background: 'rgba(59,130,246,0.04)', border: '1px solid rgba(59,130,246,0.1)' }}>
                    <h3 className="font-medium flex items-center gap-2 mb-4" style={{ color: '#3b82f6' }}>
                      <User size={18}/> Sender Info
                    </h3>
                    <div className="input-group"><label>Name</label><input value={form.senderName}    onChange={handleFieldChange('senderName')}    required /></div>
                    <div className="input-group"><label>Phone</label><input value={form.senderPhone}   onChange={handleFieldChange('senderPhone')}   required /></div>
                    <div className="input-group"><label>Street</label><input value={form.senderStreet} onChange={handleFieldChange('senderStreet')} /></div>
                    <div className="input-group"><label>City</label><input  value={form.senderCity}    onChange={handleFieldChange('senderCity')}    required /></div>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="input-group"><label>State</label><input   value={form.senderState}   onChange={handleFieldChange('senderState')} /></div>
                      <div className="input-group"><label>Pincode</label><input value={form.senderPincode} onChange={handleFieldChange('senderPincode')} required /></div>
                    </div>
                    <div className="input-group"><label>Country</label><input value={form.senderCountry} onChange={handleFieldChange('senderCountry')} /></div>
                  </div>

                  {/* Receiver */}
                  <div className="space-y-4 p-5 rounded-xl" style={{ background: 'rgba(16,185,129,0.04)', border: '1px solid rgba(16,185,129,0.1)' }}>
                    <h3 className="font-medium flex items-center gap-2 mb-4" style={{ color: '#10b981' }}>
                      <MapPin size={18}/> Receiver Info
                    </h3>
                    <div className="input-group"><label>Name</label><input    value={form.receiverName}    onChange={handleFieldChange('receiverName')}    required /></div>
                    <div className="input-group"><label>Phone</label><input   value={form.receiverPhone}   onChange={handleFieldChange('receiverPhone')}   required /></div>
                    <div className="input-group"><label>Street</label><input  value={form.receiverStreet}  onChange={handleFieldChange('receiverStreet')} /></div>
                    <div className="input-group"><label>City</label><input    value={form.receiverCity}    onChange={handleFieldChange('receiverCity')}    required /></div>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="input-group"><label>State</label><input   value={form.receiverState}   onChange={handleFieldChange('receiverState')} /></div>
                      <div className="input-group"><label>Pincode</label><input value={form.receiverPincode} onChange={handleFieldChange('receiverPincode')} required /></div>
                    </div>
                    <div className="input-group"><label>Country</label><input value={form.receiverCountry} onChange={handleFieldChange('receiverCountry')} /></div>
                  </div>
                </div>

                {/* Package Details */}
                <div className="p-5 rounded-xl" style={{ background: 'rgba(249,115,22,0.04)', border: '1px solid rgba(249,115,22,0.1)' }}>
                  <h3 className="font-medium flex items-center gap-2 mb-4" style={{ color: '#f97316' }}>
                    <Box size={18}/> Package Details
                  </h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem' }}>
                    <div className="input-group">
                      <label>Weight (kg)</label>
                      <input type="number" min="0.1" step="0.1" value={form.weightKg}
                             onChange={handleFieldChange('weightKg')} required />
                    </div>
                    <div className="input-group">
                      <label>Distance (km)</label>
                      <input type="number" min="1" step="1" value={form.distanceKm}
                             onChange={handleFieldChange('distanceKm')} required />
                    </div>
                    <div className="input-group">
                      <label>Delivery Type</label>
                      <select value={form.deliveryType} onChange={handleFieldChange('deliveryType')}>
                        <option value="NATIONAL">🇮🇳 National</option>
                        <option value="INTERNATIONAL">🌐 International</option>
                      </select>
                    </div>
                    <div className="input-group">
                      <label>Service Level</label>
                      <select value={form.serviceType} onChange={handleFieldChange('serviceType')}>
                        <option value="STANDARD">Standard</option>
                        <option value="EXPRESS">⚡ Express</option>
                      </select>
                    </div>
                  </div>
                  <div style={{ marginTop: '1rem', padding: '0.75rem 1.25rem', background: 'rgba(249,115,22,0.08)', borderRadius: '10px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Estimated Price</span>
                    <span style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f97316' }}>₹{estimatedPrice.toLocaleString('en-IN')}</span>
                  </div>
                </div>

                <div className="flex justify-end">
                  <button type="submit" className="btn-primary" disabled={saving}>
                    {saving ? 'Creating…' : <><Send size={18}/> Create Delivery</>}
                  </button>
                </div>
              </form>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── Shipping Label Modal ──────────────────────────────── */}
      <AnimatePresence>
        {selectedLabel && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="modal-overlay">
            <motion.div initial={{ scale: 0.95 }} animate={{ scale: 1 }} exit={{ scale: 0.95 }} className="modal-content">
              <div className="modal-header">
                <h3 className="font-semibold flex items-center gap-2">
                  <FileText size={18} className="text-blue-400"/> Shipping Label
                </h3>
                <button onClick={() => setSelectedLabel(null)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}>
                  <X size={20}/>
                </button>
              </div>

              <div className="modal-body">
                {/* The actual label to capture */}
                <div id="shipping-label" style={{ backgroundColor: 'white', color: 'black', padding: '2rem', borderRadius: '4px', maxWidth: '380px', margin: '0 auto', border: '1px solid #e5e7eb' }}>
                  <div style={{ textAlign: 'center', marginBottom: '1.5rem', borderBottom: '2px solid black', paddingBottom: '1rem' }}>
                    <h1 style={{ fontSize: '1.5rem', fontWeight: '900', textTransform: 'uppercase', letterSpacing: '0.1em', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
                      <Truck size={24}/> SmartCourier
                    </h1>
                    <p style={{ fontSize: '0.75rem', color: '#4b5563', fontFamily: 'monospace', marginTop: '0.25rem', marginBottom: 0 }}>
                      {selectedLabel.trackingNumber}
                    </p>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.5rem' }}>
                    <div style={{ padding: '0.5rem', border: '4px solid black', display: 'inline-block', backgroundColor: 'white' }}>
                      {/*
                        The QR code stores a tracking URL containing the tracking number.
                        In a real deployment, scanning it could directly open the tracking
                        page for this shipment.
                      */}
                      <QRCode value={`${window.location.origin}?track=${selectedLabel.trackingNumber}`} size={120} />
                    </div>
                  </div>

                  <div style={{ fontSize: '0.875rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div style={{ padding: '0.75rem', border: '2px solid black', borderRadius: '4px' }}>
                      <p style={{ fontSize: '0.65rem', fontWeight: 'bold', color: '#4b5563', textTransform: 'uppercase', letterSpacing: '0.1em', margin: '0 0 0.25rem' }}>To (Receiver)</p>
                      <p style={{ fontWeight: 'bold', fontSize: '1.125rem', margin: '0 0 0.25rem' }}>{selectedLabel.receiverAddress?.name}</p>
                      <p style={{ fontWeight: '500', margin: '0 0 0.15rem' }}>{selectedLabel.receiverAddress?.street}</p>
                      <p style={{ fontWeight: '500', margin: '0 0 0.15rem' }}>{selectedLabel.receiverAddress?.city}, {selectedLabel.receiverAddress?.state} {selectedLabel.receiverAddress?.pincode}</p>
                      <p style={{ fontWeight: '500', margin: '0 0 0.5rem' }}>{selectedLabel.receiverAddress?.country}</p>
                      <p style={{ fontSize: '0.75rem', fontWeight: 'bold', margin: 0 }}>Ph: {selectedLabel.receiverAddress?.phone}</p>
                    </div>

                    <div style={{ padding: '0.75rem' }}>
                      <p style={{ fontSize: '0.65rem', fontWeight: 'bold', color: '#4b5563', textTransform: 'uppercase', letterSpacing: '0.1em', margin: '0 0 0.25rem' }}>From (Sender)</p>
                      <p style={{ fontWeight: 'bold', margin: '0 0 0.15rem' }}>{selectedLabel.senderAddress?.name}</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500', margin: '0 0 0.15rem' }}>{selectedLabel.senderAddress?.street}</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500', margin: 0 }}>{selectedLabel.senderAddress?.city}, {selectedLabel.senderAddress?.country}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button onClick={() => setSelectedLabel(null)}
                        style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontWeight: '500', padding: '0.5rem 1rem' }}>
                  Cancel
                </button>
                <button onClick={downloadLabel} disabled={downloading} className="btn-primary" style={{ padding: '0.5rem 1.5rem', fontSize: '0.875rem' }}>
                  {downloading ? 'Generating PDF…' : 'Download PDF'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── Search bar ───────────────────────────────────────── */}
      <div className="mb-4 input-group">
        <label><Search size={16}/> Search Deliveries</label>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Tracking number, sender, receiver or city"
          className="max-w-md"
        />
      </div>

      {/* ── Filter Tabs ──────────────────────────────────────── */}
      <div className="filter-tabs">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            className={`filter-tab${activeFilter === tab.key ? ' active' : ''}`}
            onClick={() => setActiveFilter(tab.key)}
          >
            {tab.label}
            <span className="filter-tab-count">{tab.count}</span>
          </button>
        ))}
      </div>

      {/* ── Delivery cards / empty state ─────────────────────── */}
      {filteredDeliveries.length === 0 ? (
        <div className="text-center py-16 glass-card">
          <div className="no-results-icon" style={{ margin: '0 auto 1.25rem' }}>
            <Package size={36}/>
          </div>
          <h3 className="text-xl font-medium mb-2">
            {query || activeFilter !== 'all' ? 'No matching deliveries' : 'No deliveries yet'}
          </h3>
          <p style={{ color: 'var(--text-secondary)' }}>
            {query || activeFilter !== 'all'
              ? 'Try adjusting your search or filter.'
              : 'Click "New Shipment" to book your first delivery.'}
          </p>
        </div>
      ) : (
        <div className="delivery-grid">
          {filteredDeliveries.map((delivery, index) => (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
              key={delivery.id || delivery.trackingNumber}
              className="delivery-card"
            >
              <div className="delivery-card-header">
                <span className="delivery-id tracking-wider">{delivery.trackingNumber || `#${delivery.id}`}</span>
                {getStatusBadge(delivery.status)}
              </div>
              <div className="delivery-info">
                <div className="info-row">
                  <User size={16} className="text-blue-400"/>
                  <span><strong>Sender:</strong> {delivery.senderAddress?.name || 'N/A'} · {delivery.senderAddress?.city || 'Unknown'}</span>
                </div>
                <div className="info-row">
                  <MapPin size={16} className="text-green-400"/>
                  <span><strong>Receiver:</strong> {delivery.receiverAddress?.name || 'N/A'} · {delivery.receiverAddress?.city || 'Unknown'}</span>
                </div>
                <div className="info-row">
                  <Clock size={16} className="text-gray-400"/>
                  <span>
                    <strong>Date:</strong>{' '}
                    {delivery.createdAt || delivery.createdDate
                      ? new Date(delivery.createdAt || delivery.createdDate).toLocaleDateString()
                      : 'Unknown'}
                  </span>
                </div>
                {delivery.price > 0 && (
                  <div className="info-row">
                    <Box size={16} style={{ color: '#f97316' }}/>
                    <span>
                      <strong>Price:</strong> ₹{delivery.price?.toLocaleString('en-IN')}
                      {delivery.serviceType  && <span className="badge transit" style={{ marginLeft: '0.4rem', fontSize: '0.7rem', padding: '1px 6px' }}>{delivery.serviceType}</span>}
                      {delivery.deliveryType && <span className="badge transit" style={{ marginLeft: '0.3rem', fontSize: '0.7rem', padding: '1px 6px' }}>{delivery.deliveryType}</span>}
                    </span>
                  </div>
                )}
              </div>
              <div className="mt-auto pt-4" style={{ borderTop: '1px solid var(--glass-border)' }}>
                <button onClick={() => setSelectedLabel(delivery)} className="btn-outline">
                  <FileText size={16}/> View Shipping Label
                </button>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </motion.div>
  );
}
