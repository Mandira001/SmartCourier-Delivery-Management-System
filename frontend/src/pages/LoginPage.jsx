import { useState } from 'react';
import { login } from '../api.js';
import { motion } from 'framer-motion';
import { Mail, Lock, ArrowRight, Package } from 'lucide-react';

export function LoginPage({ onLogin, onMessage, onNavigate }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    /*
     * A normal HTML form submission reloads the browser page.
     * Since this is a React single-page application, we prevent that default
     * behavior and handle login through JavaScript. This keeps the user inside
     * the same app screen and allows us to show loading/error messages.
     */
    event.preventDefault();
    setLoading(true);
    try {
      /*
       * The login() function calls auth-service through the API Gateway.
       * If credentials are valid, backend returns a JWT token. That token is
       * needed for all protected requests like creating deliveries or opening
       * the admin dashboard.
       */
      const token = await login(email, password);
      /*
       * LoginPage does not store the token itself because other pages also need
       * it. The token is passed to App through onLogin, and App stores it in
       * global state/localStorage and redirects the user based on role.
       */
      onLogin(token);
    } catch (error) {
      onMessage(error.message || 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout fade-in">
      {/* Left Visual Side */}
      <div className="auth-visual">
        <div className="auth-visual-overlay"></div>
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="auth-visual-content"
        >
          <div style={{ display: 'inline-flex', padding: '12px', background: 'rgba(59, 130, 246, 0.2)', borderRadius: '16px', marginBottom: '1.5rem', border: '1px solid rgba(59, 130, 246, 0.4)' }}>
            <Package size={32} color="#60a5fa" />
          </div>
          <h2>Secure Your Supply Chain.</h2>
          <p>Log in to access your enterprise dashboard. Track shipments, manage deliveries, and monitor real-time analytics with sub-second latency.</p>
        </motion.div>
      </div>

      {/* Right Form Side */}
      <div className="auth-form-container">
        <motion.div 
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.1 }}
          className="auth-form-wrapper"
        >
          <div className="auth-header">
            <h1>Welcome Back</h1>
            <p>Sign in to manage your deliveries.</p>
          </div>

          <form className="form-grid" onSubmit={handleSubmit}>
            <div className="input-group">
              <label htmlFor="email"><Mail size={16} /> Email address</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="admin@smartcourier.com"
                required
              />
            </div>

            <div className="input-group">
              <label htmlFor="password"><Lock size={16} /> Password</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Enter your password"
                required
              />
            </div>

            <button type="submit" className="btn-primary" style={{ marginTop: '1rem', width: '100%' }} disabled={loading}>
              {loading ? 'Authenticating...' : (
                <>Sign In to Dashboard <ArrowRight size={18} /></>
              )}
            </button>
            
            <p style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.95rem', color: 'var(--text-secondary)' }}>
              Don't have an account?{' '}
              <button 
                type="button" 
                onClick={() => onNavigate('signup')} 
                style={{ background: 'none', border: 'none', color: '#60a5fa', fontWeight: 600, cursor: 'pointer', padding: 0 }}
              >
                Create one now
              </button>
            </p>
          </form>
        </motion.div>
      </div>
    </div>
  );
}
