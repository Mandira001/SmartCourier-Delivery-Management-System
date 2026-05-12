import { useState } from 'react';
import { signup } from '../api.js';
import { motion } from 'framer-motion';
import { Mail, Lock, User, UserPlus, Phone, MapPin, Building, KeyRound } from 'lucide-react';

export function SignupPage({ onMessage, onNavigate }) {
  /*
   * All signup inputs are stored in one form object.
   * This is easier than creating a separate useState for every field because
   * the complete object can be sent directly to the signup API.
   */
  const [form, setForm] = useState({
    name: '', email: '', password: '', phone: '', address: '', role: 'USER', adminKey: '',
  });
  const [loading, setLoading] = useState(false);

  /*
   * This dynamic change handler works for all inputs.
   * Each input has a `name` attribute matching a key in the form object.
   * Example: if name="email", only form.email is updated.
   */
  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (event) => {
    /*
     * Prevent the browser from doing a traditional form submit.
     * React sends the data through api.js, so the page does not reload and
     * success/failure can be shown through the shared message system.
     */
    event.preventDefault();
    setLoading(true);
    try {
      /*
       * signup(form) sends the form object to auth-service.
       * Backend is responsible for checking duplicate email, encoding password,
       * and assigning USER or ADMIN role.
       */
      await signup(form);
      onMessage('Account created successfully. Please login.');
      /*
       * Signup creates an account but does not automatically login the user.
       * After successful registration, the user is sent to LoginPage to receive
       * a JWT token through the normal login flow.
       */
      onNavigate('login');
    } catch (error) {
      onMessage(error.message || 'Signup failed.');
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
          <div style={{ display: 'inline-flex', padding: '12px', background: 'rgba(16, 185, 129, 0.2)', borderRadius: '16px', marginBottom: '1.5rem', border: '1px solid rgba(16, 185, 129, 0.4)' }}>
            <Building size={32} color="#34d399" />
          </div>
          <h2>Join the Network.</h2>
          <p>Create your account today and gain immediate access to our global enterprise delivery network. Seamlessly manage all your shipments in one place.</p>
        </motion.div>
      </div>

      {/* Right Form Side */}
      <div className="auth-form-container">
        <motion.div 
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.1 }}
          className="auth-form-wrapper"
          style={{ maxWidth: '500px' }}
        >
          <div className="auth-header">
            <h1>Create Account</h1>
            <p>Enter your details to get started.</p>
          </div>

          <form className="form-grid" onSubmit={handleSubmit}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div className="input-group">
                <label htmlFor="name"><User size={16} /> Full Name</label>
                <input
                  id="name" name="name" type="text"
                  value={form.name} onChange={handleChange}
                  placeholder="John Doe" required
                />
              </div>

              <div className="input-group">
                <label htmlFor="email"><Mail size={16} /> Email Address</label>
                <input
                  id="email" name="email" type="email"
                  value={form.email} onChange={handleChange}
                  placeholder="john@example.com" required
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div className="input-group">
                <label htmlFor="password"><Lock size={16} /> Password</label>
                <input
                  id="password" name="password" type="password"
                  value={form.password} onChange={handleChange}
                  placeholder="Create a secure password" required
                />
              </div>

              <div className="input-group">
                <label htmlFor="phone"><Phone size={10} /> Phone Number</label>
                <input
                  id="phone" name="phone" type="text"
                  value={form.phone} onChange={handleChange}
                  placeholder="+1 234 567 8900" required
                />
              </div>
            </div>

            <div className="input-group">
              <label htmlFor="address"><MapPin size={16} /> Full Address</label>
              <textarea
                id="address" name="address"
                value={form.address} onChange={handleChange}
                placeholder="123 Main St, City, Country" required
                style={{ resize: 'none', height: '80px' }}
              />
            </div>

            <div className="input-group">
              <label htmlFor="role">Account Type</label>
              <select id="role" name="role" value={form.role} onChange={handleChange} required>
                <option value="USER">Personal / Business User</option>
                <option value="ADMIN">Administrator</option>
              </select>
            </div>

            {form.role === 'ADMIN' && (
              <div className="input-group">
                <label htmlFor="adminKey"><KeyRound size={16} /> Admin Secret Key</label>
                <input
                  id="adminKey" name="adminKey" type="password"
                  value={form.adminKey} onChange={handleChange}
                  placeholder="Enter admin secret key" required
                />
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                  Contact your system administrator for the secret key.
                </p>
              </div>
            )}

            <button type="submit" className="btn-primary" style={{ marginTop: '1rem', width: '100%' }} disabled={loading}>
              {loading ? 'Creating Account...' : (
                <>Sign Up <UserPlus size={18} /></>
              )}
            </button>
            
            <p style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.95rem', color: 'var(--text-secondary)' }}>
              Already have an account?{' '}
              <button 
                type="button" 
                onClick={() => onNavigate('login')} 
                style={{ background: 'none', border: 'none', color: '#60a5fa', fontWeight: 600, cursor: 'pointer', padding: 0 }}
              >
                Sign in instead
              </button>
            </p>
          </form>
        </motion.div>
      </div>
    </div>
  );
}
