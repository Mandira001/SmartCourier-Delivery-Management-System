import { useState } from 'react';
import { getTracking } from '../api.js';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Search, MapPin, PackageCheck, Truck, CheckCircle, Clock,
  AlertTriangle, FileText, Package, ArrowRight,
} from 'lucide-react';
import { formatDate } from '../utils/date.js';

/*
 * DELIVERY_STAGES drives the visual progress tracker shown after a successful
 * tracking lookup. Each stage maps to a range of backend status strings.
 */
const DELIVERY_STAGES = [
  { key: 'booked',    label: 'Booked',           Icon: Package      },
  { key: 'picked',    label: 'Picked Up',         Icon: PackageCheck },
  { key: 'transit',   label: 'In Transit',        Icon: Truck        },
  { key: 'out',       label: 'Out for Delivery',  Icon: MapPin       },
  { key: 'delivered', label: 'Delivered',         Icon: CheckCircle  },
];

/*
 * Maps a backend status string to one of the five visual stage indices (0-4).
 * Returns -1 when the status is unknown so no stage is highlighted.
 */
const getActiveStageIndex = (status) => {
  const s = (status || '').toLowerCase();
  if (s.includes('delivered'))                             return 4;
  if (s.includes('out'))                                   return 3;
  if (s.includes('transit'))                               return 2;
  if (s.includes('picked') || s.includes('pickup'))        return 1;
  if (s.includes('booked') || s.includes('pending') || s.includes('processing')) return 0;
  return -1;
};

export function TrackingPage({ token, onMessage }) {
  const [trackingNumber, setTrackingNumber] = useState('');
  const [history, setHistory]               = useState([]);
  const [loading, setLoading]               = useState(false);
  const [searched, setSearched]             = useState(false);

  const handleSearch = async (event) => {
    /*
     * The tracking search is handled inside React instead of submitting a
     * traditional form. This lets us show loading state, errors, and the result
     * timeline without refreshing the browser.
     */
    event.preventDefault();
    if (!trackingNumber.trim()) return;

    setLoading(true);
    setSearched(true);
    try {
      /*
       * getTracking() calls tracking-service through the gateway.
       * The backend returns all tracking events saved for this tracking number,
       * including events created automatically by RabbitMQ listener.
       */
      const result = await getTracking(trackingNumber.trim(), token);
      /*
       * The UI always renders an array using map().
       * This defensive conversion protects the page if the backend ever returns
       * a single object instead of a list.
       */
      const data = Array.isArray(result) ? result : [result].filter(Boolean);

      /*
       * Sort tracking events in descending order.
       * This makes the newest update appear first, so history[0] can be used
       * as the current package status.
       */
      import('../utils/date.js').then(({ getRawDate }) => {
        data.sort((a, b) => {
          const dateA = getRawDate(a.eventDate || a.createdAt || a.timestamp);
          const dateB = getRawDate(b.eventDate || b.createdAt || b.timestamp);
          return dateB - dateA;
        });
        /*
         * Tracking events are sorted newest-first so the first item becomes the
         * current status. The same sorted array is also used to render the full
         * timeline, keeping current status and history consistent.
         */
        setHistory(data);
      });
    } catch (error) {
      setHistory([]);
      onMessage(error.message || 'Unable to load tracking history.');
    } finally {
      setLoading(false);
    }
  };

  /*
   * currentStatus is derived from the first tracking event because history is
   * sorted in descending timestamp order. If there are no events, the UI shows
   * no active status.
   */
  const currentStatus = history.length ? history[0].status || history[0].event : null;
  const activeStage   = getActiveStageIndex(currentStatus);

  const getStatusIcon = (status) => {
    /*
     * Status icons make the tracking screen easier to understand visually.
     * Delivered uses a success icon, in-transit uses a truck icon, exceptions
     * use a warning icon, and other states use a generic package icon.
     */
    const s = (status || '').toLowerCase();
    if (s.includes('delivered'))                              return <CheckCircle  size={28} className="text-green-500" />;
    if (s.includes('transit'))                                return <Truck        size={28} className="text-blue-500"  />;
    if (s.includes('exception') || s.includes('failed'))      return <AlertTriangle size={28} className="text-red-500"  />;
    return <PackageCheck size={28} className="text-orange-400" />;
  };

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="fade-in">

      {/* ── Hero Search Section ─────────────────────────────── */}
      <div className="tracking-hero">
        <div className="tracking-hero-blob"  aria-hidden="true" />
        <div className="tracking-hero-blob2" aria-hidden="true" />

        <div style={{ position: 'relative', zIndex: 1 }}>
          <div className="tracking-hero-badge">
            <MapPin size={13} /> Live Package Tracking
          </div>
          <h1 className="tracking-hero-title">Track Your Package</h1>
          <p className="tracking-hero-subtitle">Real-time updates and full delivery progress.</p>

          <form onSubmit={handleSearch} className="tracking-search-bar">
            <Search size={20} className="tracking-search-icon" />
            <input
              id="tracking"
              value={trackingNumber}
              onChange={(e) => setTrackingNumber(e.target.value)}
              placeholder="Enter tracking number, e.g. SC-1234-5678"
              className="tracking-search-input"
            />
            <button
              type="submit"
              className="tracking-search-btn"
              disabled={loading || !trackingNumber.trim()}
            >
              {loading ? 'Searching…' : <><span>Track</span><ArrowRight size={16} /></>}
            </button>
          </form>
        </div>
      </div>

      {/* ── Results ────────────────────────────────────────── */}
      <AnimatePresence mode="wait">
        {searched && (
          <motion.div
            key="results"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
          >
            {history.length > 0 ? (
              <div className="flex flex-col gap-8">

                {/* Stage Progress Tracker */}
                <div className="glass-card delivery-stages-card">
                  <p className="delivery-stages-label">Delivery Progress</p>
                  <div className="delivery-stages">
                    {DELIVERY_STAGES.map(({ key, label, Icon }, index) => {
                      const isCompleted = index < activeStage;
                      const isActive    = index === activeStage;
                      return (
                        <div key={key} className={`stage-step${isCompleted ? ' completed' : ''}${isActive ? ' active' : ''}`}>
                          {/* Left connector line */}
                          <div style={{ display: 'flex', alignItems: 'center', width: '100%', justifyContent: 'center', position: 'relative' }}>
                            <div className={`stage-connector${index === 0 ? '' : (isCompleted || isActive) ? ' filled' : ''}`}
                                 style={{ visibility: index === 0 ? 'hidden' : 'visible' }} />
                            <div className="stage-icon-wrap">
                              <Icon size={18} />
                            </div>
                            <div className={`stage-connector${index < activeStage ? ' filled' : ''}`}
                                 style={{ visibility: index === DELIVERY_STAGES.length - 1 ? 'hidden' : 'visible' }} />
                          </div>
                          <span className="stage-label">{label}</span>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Status + Timeline */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

                  {/* Current Status Card */}
                  <div className="lg:col-span-1">
                    <div className="glass-card sticky top-24" style={{ display: 'flex', flexDirection: 'column', gap: '0' }}>
                      <h2 className="text-xl font-semibold mb-4">Current Status</h2>
                      <div className="status-display">
                        <div className="status-icon-ring">
                          {getStatusIcon(currentStatus)}
                        </div>
                        <h3 className="status-name">{currentStatus || 'Processing'}</h3>
                        <p className="status-tracking-num">{trackingNumber}</p>
                      </div>
                      <div className="status-meta">
                        <div className="status-meta-item">
                          <Clock size={14} />
                          <span>Last Updated: {formatDate(history[0]?.eventDate || history[0]?.createdAt || history[0]?.timestamp)}</span>
                        </div>
                        {(history[0]?.location || history[0]?.station) && (
                          <div className="status-meta-item">
                            <MapPin size={14} />
                            <span>{history[0]?.location || history[0]?.station}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Timeline */}
                  <div className="lg:col-span-2 glass-card">
                    <h2 className="text-xl font-semibold mb-6">Tracking History</h2>
                    <div className="timeline">
                      {history.map((item, index) => {
                        /*
                         * Different backend DTOs may use different timestamp names.
                         * This fallback chain lets the same UI support eventDate,
                         * createdAt, or timestamp without breaking.
                         */
                        const dateStr      = item.eventDate || item.createdAt || item.timestamp;
                        const formattedDate = formatDate(dateStr);
                        return (
                          <motion.div
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: index * 0.08 }}
                            key={index}
                            className="timeline-item"
                          >
                            <div className="timeline-content">
                              <div className="timeline-date flex items-center gap-2">
                                <Clock size={14} className="text-blue-400" />
                                {formattedDate}
                              </div>
                              <div className="timeline-status mt-1">
                                {item.status || item.event || 'Update'}
                              </div>
                              {(item.description || item.notes) && (
                                <div className="timeline-desc mt-3 flex items-start gap-2 timeline-note">
                                  <FileText size={15} className="shrink-0 mt-0.5" />
                                  <span>{item.description || item.notes}</span>
                                </div>
                              )}
                              {(item.location || item.station) && (
                                <div className="timeline-desc mt-2 flex items-center gap-2 timeline-location">
                                  <MapPin size={15} />
                                  <span>{item.location || item.station}</span>
                                </div>
                              )}
                            </div>
                          </motion.div>
                        );
                      })}
                    </div>
                  </div>

                </div>
              </div>
            ) : (
              /* No results state */
              <div className="glass-card text-center py-16">
                <div className="no-results-icon">
                  <AlertTriangle size={36} />
                </div>
                <h3 className="text-xl font-medium mb-2 mt-5">No Tracking Information Found</h3>
                <p style={{ color: 'var(--text-secondary)' }}>
                  Please verify the tracking number and try again.
                </p>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                  Format: <span style={{ fontFamily: 'monospace', color: 'var(--accent-primary)' }}>SC-XXXX-XXXX</span>
                </p>
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}
