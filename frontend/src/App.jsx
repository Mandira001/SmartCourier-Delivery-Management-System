import { useEffect, useState } from 'react';
import { decodeJwt } from './api.js';
import { HomePage } from './pages/HomePage.jsx';
import { LoginPage } from './pages/LoginPage.jsx';
import { SignupPage } from './pages/SignupPage.jsx';
import { TrackingPage } from './pages/TrackingPage.jsx';
import { DeliveriesPage } from './pages/DeliveriesPage.jsx';
import { AdminPage } from './pages/AdminPage.jsx';
import { NotificationsPage } from './pages/NotificationsPage.jsx';
import { getNotifications } from './utils/notifications.js';
import { Package, MapPin, Truck, LayoutDashboard, LogOut, LogIn, UserPlus, Bell, Home, Sun, Moon } from 'lucide-react';
import { AnimatePresence, motion } from 'framer-motion';

const pages = [
  /*
   * This array works like a small navigation configuration.
   * Instead of writing separate JSX for every menu item, each page is described
   * with a key, label, and icon. Later, the navbar maps over this array and
   * renders buttons dynamically. This makes the navigation easier to maintain
   * because adding/removing a page mostly means changing this list.
   */
  { key: 'home', label: 'Home', icon: Home },
  { key: 'login', label: 'Login', icon: LogIn },
  { key: 'signup', label: 'Signup', icon: UserPlus },
  { key: 'tracking', label: 'Tracking', icon: MapPin },
  { key: 'deliveries', label: 'My Deliveries', icon: Truck },
  { key: 'admin', label: 'Dashboard', icon: LayoutDashboard }
];

const pageVariants = {
  initial: { opacity: 0, y: 15 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.3 } },
  exit: { opacity: 0, y: -15, transition: { duration: 0.2 } }
};

function App() {
  /*
   * The App component owns global frontend state.
   *
   * token:
   *   Stores the JWT returned by auth-service after login. Protected API calls
   *   need this token in the Authorization header.
   *
   * userRole:
   *   Stores USER or ADMIN. The role is used only to decide what UI should be
   *   visible, such as whether the admin dashboard button should appear.
   *
   * localStorage:
   *   We restore token and role from localStorage so the user does not get
   *   logged out immediately after refreshing the browser.
   */
  const [token, setToken]       = useState(localStorage.getItem('scdm_token') || '');
  const [userRole, setUserRole] = useState(localStorage.getItem('scdm_role')  || '');
  const [page, setPage]         = useState(token ? 'tracking' : 'home');
  const [message, setMessage]   = useState('');
  const [unreadCount, setUnreadCount] = useState(0);
  const [darkMode, setDarkMode] = useState(localStorage.getItem('scdm_theme') === 'dark');

  /* Apply dark mode to <html> element whenever it changes */
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', darkMode ? 'dark' : 'light');
    localStorage.setItem('scdm_theme', darkMode ? 'dark' : 'light');
  }, [darkMode]);

  useEffect(() => {
    if (token) {
      /*
       * JWT is decoded here only to read the role for frontend display logic.
       * This is not a security mechanism. A user can modify frontend state in
       * the browser, so real permission checks must still happen in the backend.
       * In this project, the gateway validates the JWT and backend services use
       * role-based annotations like @PreAuthorize.
       */
      const payload = decodeJwt(token);
      const role = payload?.role || payload?.roles || userRole || '';
      if (role) {
        setUserRole(role.toUpperCase());
        localStorage.setItem('scdm_role', role.toUpperCase());
      }
    }
  }, [token]);

  useEffect(() => {
    if (message) {
      /*
       * Toast messages are temporary feedback messages shown after actions like
       * login, logout, delivery creation, or errors. This timer clears the
       * message after 4 seconds so old messages do not remain on the screen.
       */
      const timer = setTimeout(() => setMessage(''), 4000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  useEffect(() => {
    const updateUnread = () => {
      const notifs = getNotifications();
      setUnreadCount(notifs.filter(n => !n.read).length);
    };
    
    updateUnread();
    /*
     * Notifications are stored in localStorage, but localStorage changes do not
     * automatically update React state inside the same browser tab. To solve
     * that, notifications.js dispatches a custom event whenever notifications
     * change. App listens to that event and recalculates the unread badge count.
     */
    window.addEventListener('scdm_notification_event', updateUnread);
    return () => window.removeEventListener('scdm_notification_event', updateUnread);
  }, []);

  const onLogin = (newToken) => {
    /*
     * LoginPage only handles the login form. Once auth-service returns a token,
     * the token is passed up to App because App controls global authentication
     * state, protected page access, navbar visibility, and page redirection.
     */
    setToken(newToken);
    localStorage.setItem('scdm_token', newToken);
    const payload = decodeJwt(newToken);
    let roleToSet = 'USER';
    if (payload?.role) {
      roleToSet = payload.role.toUpperCase();
    }
    setUserRole(roleToSet);
    localStorage.setItem('scdm_role', roleToSet);
    setMessage('Logged in successfully.');
    /*
     * After login, the first screen depends on the role:
     * - ADMIN goes to the operations dashboard.
     * - USER goes to tracking, which is the common customer workflow.
     */
    setPage(roleToSet === 'ADMIN' ? 'admin' : 'tracking');
  };

  const onLogout = () => {
    /*
     * Logout must clear both places where authentication data exists:
     * - React state, so the current UI immediately changes.
     * - localStorage, so refreshing the page does not restore an old session.
     */
    setToken('');
    setUserRole('');
    localStorage.removeItem('scdm_token');
    localStorage.removeItem('scdm_role');
    setPage('login');
    setMessage('Logged out successfully.');
  };

  return (
    <div className="app-shell">
      {/* Top Contact Bar */}
      <div className="top-contact-bar">
        <div className="contact-info">
          <div className="contact-item">
            <MapPin size={14} />
            <span>123 Logistic Hub, Phagwara, Punjab</span>
          </div>
          <div className="contact-item">
            <Truck size={14} />
            <span>Mon-Sat: 8 AM - 6 PM</span>
          </div>
        </div>
        <div className="contact-info">
          <span>Toll Free: <strong>+x xxx xxx xxx</strong></span>
        </div>
      </div>

      <header className="topbar">
        <div className="brand">
          <Package className="text-blue-500" size={28} color="#3b82f6" />
          <span>SmartCourier</span>
        </div>
        <nav className="nav-links">
          {pages.map((item) => {
            /*
             * Role-based navigation improves the user experience by hiding
             * screens that are not relevant. For example, an ADMIN sees the
             * dashboard and a logged-out visitor sees login/signup.
             *
             * Important: this is only frontend visibility control. Backend
             * APIs are still protected separately using JWT and role checks.
             */
            if (item.key === 'admin'       && userRole !== 'ADMIN') return null;
            if (item.key === 'deliveries'  && userRole === 'ADMIN') return null; // Admin cannot create deliveries
            if ((item.key === 'tracking' || item.key === 'deliveries') && !token) return null;
            if ((item.key === 'login' || item.key === 'signup') && token) return null;
            if (item.key === 'home' && token) return null;
            
            const Icon = item.icon;
            return (
              <button
                key={item.key}
                className={page === item.key ? 'nav-button active' : 'nav-button'}
                onClick={() => setPage(item.key)}
              >
                <Icon size={18} />
                <span className="nav-label">{item.label}</span>
              </button>
            );
          })}
        </nav>
        <div className="auth-actions">
          {/* Dark / Light mode toggle */}
          <button
            className="theme-toggle"
            onClick={() => setDarkMode(d => !d)}
            title={darkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
          >
            {darkMode ? <Sun size={17} /> : <Moon size={17} />}
          </button>

          {token && (
            <>
              <button
                className={page === 'notifications' ? 'nav-button active' : 'nav-button'}
                onClick={() => setPage('notifications')}
                style={{ position: 'relative' }}
                title="Notifications"
              >
                <Bell size={18} />
                {unreadCount > 0 && (
                  <span style={{
                    position: 'absolute', top: '0', right: '0', background: '#ef4444', color: 'white',
                    fontSize: '10px', fontWeight: 'bold', width: '16px', height: '16px',
                    borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
                    transform: 'translate(25%, -25%)', boxShadow: '0 0 8px rgba(239, 68, 68, 0.8)'
                  }}>
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              <div className="user-badge" style={{ marginLeft: '0.5rem' }}>
                <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#10b981', boxShadow: '0 0 8px #10b981' }} />
                {userRole || 'USER'}
              </div>
              <button className="nav-button" style={{ color: '#ef4444' }} onClick={onLogout}>
                <LogOut size={18} />
                <span className="nav-label">Logout</span>
              </button>
            </>
          )}
        </div>
      </header>

      <main className={page === 'home' ? 'page-container p-0' : 'page-container'}>
        <AnimatePresence mode="wait">
          {message && (
            <motion.div 
              key="toast"
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="toast"
            >
              {message}
            </motion.div>
          )}
        </AnimatePresence>

        <AnimatePresence mode="wait">
          <motion.div
            key={page}
            variants={pageVariants}
            initial="initial"
            animate="animate"
            exit="exit"
            className="w-full flex-1 flex flex-col"
          >
            {/*
              This project uses simple state-based navigation instead of
              React Router. The `page` state stores the active screen name, and
              only the matching page component is mounted. This is enough here
              because the app has a small number of screens and does not depend
              on deep browser URLs.
            */}
            {page === 'home' && <HomePage onNavigate={setPage} />}
            {page === 'login' && <LoginPage onLogin={onLogin} onMessage={setMessage} onNavigate={setPage} />}
            {page === 'signup' && <SignupPage onMessage={setMessage} onNavigate={setPage} />}
            {page === 'tracking' && <TrackingPage token={token} onMessage={setMessage} />}
            {page === 'deliveries' && <DeliveriesPage token={token} onMessage={setMessage} />}
            {page === 'admin' && <AdminPage token={token} onMessage={setMessage} />}
            {page === 'notifications' && <NotificationsPage />}
          </motion.div>
        </AnimatePresence>
      </main>
    </div>
  );
}

export default App;
