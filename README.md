# emessageme
Android Email-Like Messaging app powered by Firebase

Developed for Mobile Application Development, Spring 2024

## Requirements: 

* Mailbox:
  * List of messages sent/received by current logged in user. Consider color coding sent/received messages
  * Messages should have: sender, receiver, title, text, creation date, opened
  * Messages can be deleted, but doesn't affect other user's copy of message
  * Search for messages (Firebase Firestore Query)
 * Create Message:
   * Create new message by entering title, text, select receiver from list of all users that did not block sender
   * After sending, message should appear in both sender and receiver mailboxes
 * Reply to message:
   * Create reply to message by entering text (title and receiver remain same)
   * After sending, message should appear in both sender and receiver mailboxes, with indication it's a reply
 * User listing:
   * Shows ALL users in system, with option to block/unblock from user's messages
   * User should be able to view list of blocked users
