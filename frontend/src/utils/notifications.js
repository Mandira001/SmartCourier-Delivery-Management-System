export const getNotifications = () => {
  try {
    /*
     * Notifications are simulated using browser localStorage.
     * This lets the project demonstrate email/SMS style alerts without needing
     * real external integrations like SMTP, Twilio, or Firebase.
     */
    return JSON.parse(localStorage.getItem('scdm_notifications') || '[]');
  } catch {
    /*
     * If localStorage contains invalid JSON for any reason, the app should not
     * crash. Returning an empty list keeps the notification page stable.
     */
    return [];
  }
};

export const notifyUser = (notification) => {
  const existing = getNotifications();
  /*
   * Pages pass only the notification content, like title/message/type.
   * This function adds frontend metadata such as id, timestamp, and read status
   * before storing the notification in localStorage.
   */
  const newNotif = {
    id: Date.now().toString() + Math.random().toString(36).substring(7),
    date: new Date().toISOString(),
    read: false,
    ...notification
  };
  
  localStorage.setItem('scdm_notifications', JSON.stringify([newNotif, ...existing]));
  
  /*
   * Dispatch a custom event so components that read notification state, such
   * as the topbar unread badge and NotificationsPage, update instantly.
   */
  window.dispatchEvent(new Event('scdm_notification_event'));
};

export const markAsRead = (id) => {
  /*
   * When a user opens a notification, only that notification should become read.
   * map() creates a new updated list while leaving all other notifications as
   * they were.
   */
  const existing = getNotifications();
  const updated = existing.map(n => n.id === id ? { ...n, read: true } : n);
  localStorage.setItem('scdm_notifications', JSON.stringify(updated));
  window.dispatchEvent(new Event('scdm_notification_event'));
};

export const markAllAsRead = () => {
  /*
   * This supports inbox-like behavior. Marking all as read clears the unread
   * count shown in the top navigation badge.
   */
  const existing = getNotifications();
  const updated = existing.map(n => ({ ...n, read: true }));
  localStorage.setItem('scdm_notifications', JSON.stringify(updated));
  window.dispatchEvent(new Event('scdm_notification_event'));
};
