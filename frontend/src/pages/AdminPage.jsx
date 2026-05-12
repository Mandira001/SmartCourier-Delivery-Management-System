import { useEffect, useMemo, useState } from 'react';
import { getAdminDashboard, getAdminDeliveries, updateDeliveryStatus, getRevenueTrend } from '../api.js';
import { motion } from 'framer-motion';
import {
  Package, CheckCircle, Clock, AlertTriangle,
  Search, Filter, Download, Activity, TrendingUp,
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell,
  LineChart, Line, CartesianGrid,
} from 'recharts';
import { formatShortDate } from '../utils/date.js';

export function AdminPage({ token, onMessage }) {
  const [dashboard, setDashboard]       = useState(null);
  const [deliveries, setDeliveries]     = useState([]);
  const [revenueTrend, setRevenueTrend] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [search, setSearch]             = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        /*
         * Load dashboard summary and delivery list together.
         * Revenue trend is fetched separately so a missing or failing endpoint
         * does not prevent the rest of the dashboard from rendering.
         */
        const [summary, deliveryList] = await Promise.all([
          getAdminDashboard(token),
          getAdminDeliveries(token),
        ]);
        setDashboard(summary);
        setDeliveries(deliveryList || []);
      } catch (error) {
        onMessage(error.message || 'Failed to load admin data.');
      } finally {
        setLoading(false);
      }

      // Revenue trend loaded independently — chart shows empty if endpoint unavailable
      getRevenueTrend(token)
        .then(trend => setRevenueTrend(Array.isArray(trend) ? trend : []))
        .catch(() => setRevenueTrend([]));
    };
    if (token) load();
  }, [token]);

  const filteredDeliveries = useMemo(() => {
    /*
     * Admin can filter deliveries by search text and status.
     * useMemo is used here because filtering can run frequently while typing.
     * It recalculates only when deliveries, search text, or status filter change.
     */
    const normalized = search.trim().toLowerCase();
    return deliveries.filter((delivery) => {
      const matchesText = normalized
        ? [delivery.id, delivery.trackingNumber, delivery.status,
           delivery.notes, delivery.receiverAddress?.name, delivery.senderAddress?.name]
            .filter(Boolean).join(' ').toLowerCase().includes(normalized)
        : true;
      const matchesStatus = statusFilter === 'all'
        ? true
        : delivery.status?.toLowerCase().includes(statusFilter);
      return matchesText && matchesStatus;
    });
  }, [deliveries, search, statusFilter]);

  const metrics = useMemo(() => {
    const total      = deliveries.length;
    const delivered  = deliveries.filter((d) => d.status === 'DELIVERED').length;
    const pending    = deliveries.filter((d) =>
      ['BOOKED','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY'].includes(d.status)
    ).length;
    const exceptions = deliveries.filter((d) => d.status === 'EXCEPTION').length;
    return { total, delivered, pending, exceptions };
  }, [deliveries]);

  /*
   * Recharts expects simple chart-friendly objects.
   * This array converts the metrics into labels, values, and colors used by
   * the status distribution bar chart.
   */
  const chartData = [
    { name: 'Pending',    count: metrics.pending,    color: '#f59e0b' },
    { name: 'Delivered',  count: metrics.delivered,  color: '#10b981' },
    { name: 'Exceptions', count: metrics.exceptions, color: '#ef4444' },
  ];

  /*
   * Revenue trend data comes from the backend /admin/revenue-trend endpoint.
   * Each entry is { month: "2026-05", revenue: 12500 }.
   * We format the month key into a readable label like "May '26".
   */
  const revenueData = useMemo(() => {
    const formatMonth = (key) => {
      const [year, month] = key.split('-');
      const d = new Date(parseInt(year), parseInt(month) - 1, 1);
      return d.toLocaleString('default', { month: 'short' }) + " '" + year.slice(2);
    };
    return revenueTrend.map((entry) => ({
      name:    formatMonth(entry.month),
      revenue: entry.revenue,
    }));
  }, [revenueTrend]);

  const recentActivity = useMemo(() => {
    /*
     * Recent activity is derived from the delivery list by sorting the newest
     * created deliveries first and showing only the latest five. This gives the
     * admin a quick view of recent operational changes.
     */
    return [...deliveries]
      .sort((a, b) => new Date(b.createdAt || b.createdDate || 0) - new Date(a.createdAt || a.createdDate || 0))
      .slice(0, 5);
  }, [deliveries]);

  const getStatusBadge = (status) => {
    /*
     * Delivery status is stored as a backend string. This helper converts those
     * strings into colored badges so the admin can scan table rows quickly.
     */
    const s = (status || '').toLowerCase();
    if (s.includes('delivered'))
      return <span className="badge delivered"><CheckCircle size={14}/> Delivered</span>;
    if (s.includes('pending') || s.includes('transit'))
      return <span className="badge pending"><Clock size={14}/> Pending</span>;
    if (s.includes('exception') || s.includes('failed'))
      return <span className="badge" style={{ background: 'rgba(239,68,68,0.12)', color: '#ef4444', border: '1px solid rgba(239,68,68,0.3)' }}>
        <AlertTriangle size={14}/> Exception
      </span>;
    return <span className="badge transit">{status}</span>;
  };

  const handleUpdateStatus = async (id, newStatus) => {
    try {
      /*
       * Status update is sent to admin-service through the gateway.
       * Admin-service calls delivery-service using Feign. Delivery-service then
       * validates the transition, updates PostgreSQL, and publishes a tracking
       * event to RabbitMQ.
       *
       * The frontend updates local state only after the backend call succeeds.
       */
      await updateDeliveryStatus(id, newStatus, token);
      setDeliveries((prev) => prev.map((d) => d.id === id ? { ...d, status: newStatus } : d));
      onMessage(`Delivery #${id} updated to ${newStatus}`);

      /*
       * After a successful status update, the frontend creates a simulated SMS
       * alert so the demo can show customer notification behavior.
       */
      const target     = deliveries.find((d) => d.id === id);
      const trackingNo = target?.trackingNumber || `#${id}`;

      import('../utils/notifications.js').then(({ notifyUser }) => {
        /*
         * This creates a simulated SMS notification in localStorage.
         * It demonstrates how a real courier system might notify customers
         * after status changes, but no actual SMS provider is integrated here.
         */
        notifyUser({
          type: 'sms',
          title: 'Delivery Update',
          message: `Your package ${trackingNo} has been updated to: ${newStatus.replace(/_/g, ' ')}. Track it live in your dashboard.`,
          trackingNumber: trackingNo,
        });
      });
    } catch (error) {
      onMessage(error.message || 'Failed to update status.');
    }
  };

  const exportToCSV = () => {
    if (!filteredDeliveries.length) return onMessage('No data to export.');

    /*
     * CSV export uses the currently filtered delivery list.
     * This means if the admin searches or filters by status, the exported file
     * matches the table currently visible on screen.
     */
    const headers    = ['ID', 'Tracking Number', 'Status', 'Created At', 'Notes'];
    const csvContent = [
      headers.join(','),
      ...filteredDeliveries.map((d) => [
        d.id,
        d.trackingNumber,
        `"${d.status || ''}"`,
        `"${formatShortDate(d.createdAt || d.createdDate)}"`,
        `"${(d.notes || d.exception || '').replace(/"/g, '""')}"`,
      ].join(',')),
    ].join('\n');

    /*
     * The CSV is generated fully in the browser.
     * Blob creates a file-like object, URL.createObjectURL creates a temporary
     * download URL, and a hidden link click starts the download. This avoids
     * needing a separate backend endpoint for CSV generation.
     */
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url  = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href  = url;
    link.setAttribute('download', `SmartCourier_Report_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    onMessage('Report downloaded successfully.');
  };

  /* Tooltip style adapts to current theme */
  const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
  const tooltipStyle = {
    backgroundColor: isDark ? '#1e293b' : '#ffffff',
    border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)'}`,
    borderRadius: '8px',
    color: isDark ? '#f1f5f9' : '#0f172a',
    boxShadow: isDark ? '0 4px 12px rgba(0,0,0,0.5)' : '0 4px 12px rgba(0,0,0,0.08)',
    fontSize: '0.875rem',
  };
  const gridStroke = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';

  if (loading) {
    return (
      <div style={{ display: 'flex', flex: 1, alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ width: '48px', height: '48px', border: '4px solid var(--accent-primary)', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
      </div>
    );
  }

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="fade-in">

      {/* ── Page header ──────────────────────────────────────── */}
      <div className="page-header">
        <div>
          <h1>Operations Dashboard</h1>
          <p>Monitor deliveries, revenue trends, and overall service performance.</p>
        </div>
      </div>

      {/* ── Stat Cards ───────────────────────────────────────── */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        <div className="stat-card">
          <div className="stat-icon blue"><Package /></div>
          <div className="stat-content"><h3>Total Deliveries</h3><p>{metrics.total}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><CheckCircle /></div>
          <div className="stat-content"><h3>Delivered</h3><p>{metrics.delivered}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange"><Clock /></div>
          <div className="stat-content"><h3>In Transit / Pending</h3><p>{metrics.pending}</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(16,185,129,0.12)', color: '#10b981', border: '1px solid rgba(16,185,129,0.2)' }}><TrendingUp /></div>
          <div className="stat-content">
            <h3>Total Revenue</h3>
            <p style={{ color: '#10b981' }}>
              {dashboard?.totalRevenue != null
                ? `₹${Number(dashboard.totalRevenue).toLocaleString('en-IN')}`
                : '₹—'}
            </p>
          </div>
        </div>
        <div className="stat-card" style={{ borderColor: metrics.exceptions > 0 ? 'rgba(239,68,68,0.3)' : '' }}>
          <div className="stat-icon" style={{ background: 'rgba(239,68,68,0.12)', color: '#ef4444', border: '1px solid rgba(239,68,68,0.2)' }}><AlertTriangle /></div>
          <div className="stat-content">
            <h3>Exceptions</h3>
            <p style={{ color: metrics.exceptions > 0 ? '#ef4444' : 'var(--text-primary)' }}>{metrics.exceptions}</p>
          </div>
        </div>
      </div>

      {/* ── Charts ───────────────────────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>

        <div className="glass-card">
          <h2 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-primary)' }}>
            <Package size={20} color="var(--accent-primary)" /> Status Distribution
          </h2>
          <div style={{ height: '240px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={13} />
                <YAxis stroke="#94a3b8" allowDecimals={false} fontSize={12} />
                <Tooltip cursor={{ fill: 'rgba(249,115,22,0.05)' }} contentStyle={tooltipStyle} />
                <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="glass-card">
          <h2 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-primary)' }}>
            <TrendingUp size={20} color="#10b981" /> Revenue Trends
          </h2>
          <div style={{ height: '240px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={revenueData} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={gridStroke} vertical={false} />
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={13} />
                <YAxis stroke="#94a3b8" fontSize={12} tickFormatter={(v) => `₹${v / 1000}k`} />
                <Tooltip contentStyle={tooltipStyle} formatter={(v) => [`₹${v.toLocaleString()}`, 'Revenue']} />
                <Line type="monotone" dataKey="revenue" stroke="#10b981" strokeWidth={3}
                      dot={{ r: 4, fill: '#10b981', strokeWidth: 0 }} activeDot={{ r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* ── Deliveries Table + Sidebar ────────────────────────── */}
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1.5rem', marginBottom: '2rem' }}>

        {/* Table panel */}
        <div className="glass-card" style={{ flex: '2 1 600px', display: 'flex', flexDirection: 'column' }}>
          {/* Search + filter toolbar */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem', justifyContent: 'space-between', alignItems: 'flex-end' }}>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', flex: 1 }}>
              <div className="input-group" style={{ flex: '1 1 200px' }}>
                <label htmlFor="admin-search"><Search size={16} /> Search Deliveries</label>
                <input
                  id="admin-search"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Tracking number, notes..."
                />
              </div>
              <div className="input-group" style={{ flex: '0 1 200px' }}>
                <label htmlFor="admin-filter"><Filter size={16} /> Status</label>
                <select id="admin-filter" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                  <option value="all">All Statuses</option>
                  <option value="delivered">Delivered</option>
                  <option value="pending">Pending</option>
                  <option value="transit">In Transit</option>
                  <option value="exception">Exceptions / Failed</option>
                </select>
              </div>
            </div>
            <button
              onClick={exportToCSV}
              className="btn-primary"
              style={{ background: 'rgba(59,130,246,0.08)', color: '#3b82f6', border: '1px solid rgba(59,130,246,0.25)', height: '42px', padding: '0 1rem', whiteSpace: 'nowrap', boxShadow: 'none' }}
            >
              <Download size={18} /> Export CSV
            </button>
          </div>

          {/* Table */}
          <div className="table-container" style={{ flex: 1 }}>
            <table>
              <thead>
                <tr>
                  <th>Tracking Number</th>
                  <th>Status</th>
                  <th>Created At</th>
                  <th>Update Status</th>
                </tr>
              </thead>
              <tbody>
                {filteredDeliveries.length > 0 ? (
                  filteredDeliveries.map((d) => (
                    <tr key={d.id || d.trackingNumber}>
                      <td style={{ fontFamily: 'monospace', color: 'var(--accent-primary)', fontWeight: 600 }}>
                        {d.trackingNumber || `#${d.id}`}
                      </td>
                      <td>{getStatusBadge(d.status)}</td>
                      <td style={{ color: 'var(--text-secondary)' }}>{formatShortDate(d.createdAt || d.createdDate)}</td>
                      <td>
                        <select
                          className="admin-status-select"
                          value={d.status}
                          onChange={(e) => handleUpdateStatus(d.id, e.target.value)}
                        >
                          <option value="BOOKED">Booked</option>
                          <option value="PICKED_UP">Picked Up</option>
                          <option value="IN_TRANSIT">In Transit</option>
                          <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
                          <option value="DELIVERED">Delivered</option>
                          <option value="EXCEPTION">⚠ Exception</option>
                        </select>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--text-muted)' }}>
                      No deliveries found matching your search.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Sidebar */}
        <div style={{ flex: '1 1 280px', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>

          {/* Recent Activity */}
          <div className="glass-card">
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-primary)' }}>
              <Activity size={20} color="var(--accent-primary)" /> Recent Activity
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {recentActivity.length > 0 ? recentActivity.map((activity) => (
                <div key={activity.id} className="admin-activity-item">
                  <div style={{ marginTop: '2px', flexShrink: 0 }}>
                    {activity.status?.toLowerCase().includes('deliver')
                      ? <CheckCircle size={16} color="#10b981" />
                      : activity.status?.toLowerCase().includes('exception')
                        ? <AlertTriangle size={16} color="#ef4444" />
                        : <Clock size={16} color="var(--accent-primary)" />}
                  </div>
                  <div>
                    <div style={{ fontWeight: 500, fontSize: '0.875rem', color: 'var(--text-primary)' }}>
                      Delivery{' '}
                      <span style={{ color: 'var(--accent-primary)', fontFamily: 'monospace' }}>
                        {activity.trackingNumber}
                      </span>{' '}updated
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '3px', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                      <span style={{ textTransform: 'uppercase', fontWeight: 700, color: activity.status?.toLowerCase().includes('deliver') ? '#10b981' : '#f59e0b' }}>
                        {activity.status}
                      </span>
                      · {formatShortDate(activity.createdAt || activity.createdDate || Date.now())}
                    </div>
                  </div>
                </div>
              )) : (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No recent activity found.</p>
              )}
            </div>
          </div>

          {/* Backend Summary */}
          <div className="glass-card" style={{ flex: 1 }}>
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1.25rem', color: 'var(--text-primary)' }}>
              Backend Summary
            </h2>
            {dashboard ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                {Object.entries(dashboard).map(([key, value]) => (
                  <div key={key} className="admin-summary-row">
                    <span className="admin-summary-key">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
                    <span className="admin-summary-val">{value ?? '0'}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', textAlign: 'center', padding: '1rem 0' }}>
                No summary data available.
              </p>
            )}
          </div>
        </div>
      </div>
    </motion.div>
  );
}
