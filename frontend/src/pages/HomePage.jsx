import { useState } from 'react';
import { motion } from 'framer-motion';
import { Search, Globe, Zap, ShieldCheck, Clock, MapPin, ChevronRight, TrendingUp, Truck } from 'lucide-react';

export function HomePage({ onNavigate }) {
  const [trackingNumber, setTrackingNumber] = useState('');

  const handleTrack = (e) => {
    e.preventDefault();
    if (!trackingNumber.trim()) return;
    onNavigate('login');
  };

  const staggerContainer = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: { staggerChildren: 0.2 }
    }
  };

  const fadeUp = {
    hidden: { opacity: 0, y: 30 },
    show: { opacity: 1, y: 0, transition: { duration: 0.6, ease: "easeOut" } }
  };

  return (
    <div className="home-page fade-in">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-overlay"></div>
        <div className="hero-content">
          <motion.div initial="hidden" animate="show" variants={staggerContainer} className="hero-text-container">
            <motion.div variants={fadeUp} className="hero-badge">
              <Zap size={14} className="text-orange-500" />
              <span>Next-Gen Logistics Platform</span>
            </motion.div>
            
            <motion.h1 variants={fadeUp} className="hero-title">
              FAST RESPONSES <br />
              <span className="text-gradient">FAST DELIVERY</span>
            </motion.h1>
            
            <motion.p variants={fadeUp} className="hero-subtitle">
              Reliable, fast, and secure delivery management system. We ensure your packages reach their destination safely and on time.
            </motion.p>
            
            <motion.form variants={fadeUp} onSubmit={handleTrack} className="hero-tracking-bar">
              <MapPin className="tracking-icon" size={24} />
              <input 
                type="text" 
                placeholder="Enter Tracking Number (e.g. SC-1234-5678)" 
                value={trackingNumber}
                onChange={(e) => setTrackingNumber(e.target.value)}
                className="tracking-input"
              />
              <button type="submit" className="tracking-btn">
                Track Now <ChevronRight size={18} />
              </button>
            </motion.form>
          </motion.div>
          
          <motion.div 
            initial={{ opacity: 0, x: 100 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.8, ease: "easeOut", delay: 0.2 }}
            className="hero-graphic-container"
          >
            <img src="/hero-truck.png" alt="Orange Delivery Truck" className="hero-graphic" />
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <div className="features-header">
          <h4 style={{ color: '#f97316', fontWeight: 'bold', textTransform: 'uppercase', letterSpacing: '2px', marginBottom: '0.5rem' }}>Features</h4>
          <h2>What we offer</h2>
          <p>Our platform provides all the essential tools you need for seamless delivery management.</p>
        </div>
        
        <div className="features-grid">
          <motion.div initial="hidden" whileInView="show" viewport={{ once: true, margin: "-100px" }} variants={staggerContainer} className="grid-container">
            <motion.div variants={fadeUp} className="feature-card">
              <div className="feature-icon-wrapper blue">
                <MapPin size={28} />
              </div>
              <h3>Live Tracking</h3>
              <p>Track every movement of your packages in real-time using your unique tracking number.</p>
            </motion.div>
            
            <motion.div variants={fadeUp} className="feature-card">
              <div className="feature-icon-wrapper green">
                <Truck size={28} />
              </div>
              <h3>Delivery Management</h3>
              <p>Manage all your deliveries and shipments efficiently from a single, intuitive dashboard.</p>
            </motion.div>
            
            <motion.div variants={fadeUp} className="feature-card">
              <div className="feature-icon-wrapper orange">
                <ShieldCheck size={28} />
              </div>
              <h3>Role-Based Access</h3>
              <p>Secure authentication system with dedicated views for both regular users and administrators.</p>
            </motion.div>

          </motion.div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta-section">
        <div className="cta-card glass-card">
          <h2>Ready to manage your deliveries?</h2>
          <p>Join SmartCourier today to track and manage your packages efficiently.</p>
          <div className="cta-buttons">
            <button className="btn-primary" onClick={() => onNavigate('signup')}>Create Account</button>
            <button className="btn-outline" onClick={() => onNavigate('login')}>Sign In</button>
          </div>
        </div>
      </section>
      
      {/* Footer */}
      <footer className="home-footer">
        <div className="brand">
          <Truck size={20} color="#f97316" />
          <span>SmartCourier Inc.</span>
        </div>
        <p>© 2026 SmartCourier Platform. All rights reserved.</p>
      </footer>
    </div>
  );
}
