import React from 'react';

export function Badge({ text, variant = 'default' }) {
  const classes = ['badge'];
  if (variant === 'success') classes.push('badge-success');
  if (variant === 'warning') classes.push('badge-warning');
  if (variant === 'danger') classes.push('badge-danger');
  if (variant === 'info') classes.push('badge-info');
  return <span className={classes.join(' ')}>{text}</span>;
}

export function MetricCard({ label, value, detail }) {
  return (
    <div className="metric-card">
      <span className="metric-value">{value}</span>
      <span className="metric-label">{label}</span>
      {detail ? <small className="metric-detail">{detail}</small> : null}
    </div>
  );
}

export function SectionHeader({ title, description }) {
  return (
    <div className="section-header">
      <div>
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
    </div>
  );
}
