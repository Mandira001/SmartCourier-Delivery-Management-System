export const formatDate = (dateInput) => {
  /*
   * Converts backend date values into a full readable date/time string.
   * This is used in places like tracking timelines and notification views where
   * users need to know the exact update time.
   */
  if (!dateInput) return 'Date pending';

  if (Array.isArray(dateInput)) {
    /*
     * Spring/Java date-time values can sometimes reach the frontend as arrays:
     * [year, month, day, hour, minute, second].
     * This block converts that array into a JavaScript Date.
     */
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateInput;
    const d = new Date(Date.UTC(year, month - 1, day, hour, minute, second));
    return d.toLocaleString();
  }

  let dateStr = dateInput;
  if (typeof dateStr === 'string' && !dateStr.endsWith('Z') && !dateStr.includes('+')) {
    /*
     * Some backend strings may not include timezone information.
     * Adding Z treats them as UTC and avoids different browsers interpreting
     * the same string differently.
     */
    dateStr += 'Z';
  }

  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return 'Date pending';
  
  return d.toLocaleString();
};

export const formatShortDate = (dateInput) => {
  /*
   * Converts backend date values into a short date string.
   * This is used in dense UI areas like tables and CSV export, where full
   * timestamp detail would make the layout harder to scan.
   */
  if (!dateInput) return '—';

  if (Array.isArray(dateInput)) {
    /*
     * Supports the same Java array-style date format, but only year/month/day
     * are needed for short date display.
     */
    const [year, month, day] = dateInput;
    const d = new Date(Date.UTC(year, month - 1, day));
    return d.toLocaleDateString();
  }

  let dateStr = dateInput;
  if (typeof dateStr === 'string' && !dateStr.endsWith('Z') && !dateStr.includes('+')) {
    /*
     * Normalize date strings before parsing so date display is consistent
     * across browser environments.
     */
    dateStr += 'Z';
  }

  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return '—';
  
  return d.toLocaleDateString();
};

export const getRawDate = (dateInput) => {
  /*
   * Returns a raw JavaScript Date object instead of formatted text.
   * TrackingPage uses this for sorting events, because sorting should compare
   * actual time values rather than already-formatted strings.
   */
  if (!dateInput) return new Date(0);
  if (Array.isArray(dateInput)) {
    /*
     * Same Java LocalDateTime array support as formatDate, but returns Date
     * directly for comparison.
     */
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateInput;
    return new Date(Date.UTC(year, month - 1, day, hour, minute, second));
  }
  
  let dateStr = dateInput;
  if (typeof dateStr === 'string' && !dateStr.endsWith('Z') && !dateStr.includes('+')) {
    /*
     * Add UTC marker when backend does not include timezone info.
     * This keeps sorting consistent for timestamp strings.
     */
    dateStr += 'Z';
  }
  return new Date(dateStr);
};
