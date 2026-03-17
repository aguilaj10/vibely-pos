# Vibely POS - Testing Guide

**Version:** 1.0.0  
**Last Updated:** March 16, 2026  
**Target Audience:** QA Engineers, Testers

---

## Table of Contents

1. [Overview](#overview)
2. [Test Environment Setup](#test-environment-setup)
3. [Keyboard Navigation Testing (Desktop)](#keyboard-navigation-testing-desktop)
4. [Screen Reader Testing](#screen-reader-testing)
5. [Cross-Platform Testing](#cross-platform-testing)
6. [Load Testing](#load-testing)
7. [Security Penetration Testing](#security-penetration-testing)
8. [Test Reporting](#test-reporting)

---

## Overview

This guide provides step-by-step instructions for manual and automated testing of the Vibely POS system across multiple areas:

- **Keyboard Navigation** - Accessibility compliance for keyboard-only users
- **Screen Reader Compatibility** - TalkBack (Android) and VoiceOver (iOS) testing
- **Cross-Platform Verification** - Desktop, Android, iOS, Web functionality
- **Load Testing** - Performance under concurrent user load (100 users)
- **Security Testing** - OWASP Top 10 vulnerability assessment

**Expected Outcome:** All tests documented, issues logged, and verification that the application meets production-ready quality standards.

---

## Test Environment Setup

### Prerequisites

**For All Tests:**
- Access to Supabase test database (separate from production)
- Test user accounts with different roles (admin, manager, cashier, warehouse, viewer)
- Backend server running on test environment
- Test data populated (products, customers, sales)

**For Desktop Testing:**
- Windows 10/11, macOS 12+, Ubuntu 22.04 LTS
- JDK 17+ installed
- Desktop application installed from `/composeApp/build/compose/binaries/`

**For Mobile Testing:**
- Android device (API 24-34) or emulator
- iOS device (iOS 15+) or simulator
- Mobile apps installed from `/composeApp/build/outputs/`

**For Load Testing:**
- Apache JMeter 5.5+ or Gatling 3.9+
- Test scripts configured with API endpoints
- Ability to generate 100 concurrent users

**For Security Testing:**
- OWASP ZAP 2.12+ or Burp Suite
- Network access to backend API
- Valid JWT tokens for authenticated endpoints

### Environment Configuration

Create test environment variables (`.env.test`):

```bash
SUPABASE_URL=https://your-test-project.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your-test-key
JWT_SECRET=test-jwt-secret-do-not-use-in-prod
PORT=8080
HOST=0.0.0.0
ENFORCE_HTTPS=false
DEBUG_MODE=false
```

### Test Data Setup

Run test data seed script (if available) or manually create:

- 50+ products across 5 categories
- 20+ customers with varying loyalty tiers
- 10+ suppliers
- 5+ users with different roles
- 30+ historical sales transactions
- 10+ active purchase orders
- 3+ open/closed cash shifts

---

## Keyboard Navigation Testing (Desktop)

### Objective
Verify that all interactive elements are accessible and functional using only the keyboard (no mouse).

### Test Environment
- **Platform:** Desktop (Windows, macOS, Linux)
- **Tools:** Keyboard only (Tab, Shift+Tab, Enter, Space, Arrow keys, Esc)
- **Success Criteria:** All features accessible via keyboard, visible focus indicators, logical tab order

### Test Scenarios

#### 1. Login Screen Navigation

**Steps:**
1. Launch application
2. Press `Tab` repeatedly to cycle through elements
3. Verify tab order: Email field → Password field → "Remember Me" checkbox → "Sign In" button
4. Enter email using keyboard
5. Press `Tab` to move to password field
6. Enter password
7. Press `Enter` to submit login form

**Expected Results:**
- ✅ Focus indicator visible on each element
- ✅ Tab order is logical (top to bottom, left to right)
- ✅ Enter key submits form without clicking button
- ✅ Error messages announced on invalid input

**Pass/Fail:** ________  **Notes:** ________________

---

#### 2. Dashboard Navigation

**Steps:**
1. After login, verify focus on dashboard
2. Press `Tab` to navigate through:
   - Metric cards (Revenue, Transactions, etc.)
   - Recent transactions table rows
   - Quick action buttons (New Sale, Add Product, etc.)
3. Press `Enter` on "New Sale" quick action
4. Verify navigation to Checkout screen

**Expected Results:**
- ✅ All interactive elements receive focus
- ✅ Focus indicators clearly visible (outline/highlight)
- ✅ Enter/Space activate buttons and links
- ✅ Table rows are keyboard navigable

**Pass/Fail:** ________  **Notes:** ________________

---

#### 3. Checkout Screen (Point of Sale)

**Steps:**
1. Navigate to Checkout screen
2. Press `Tab` to focus product search field
3. Type product name or SKU
4. Press `Arrow Down` to navigate search results
5. Press `Enter` to select product
6. Use `Tab` to navigate quantity field
7. Enter quantity, press `Tab` to add to cart
8. Navigate cart items with `Tab`
9. Verify `Delete` button receives focus on cart items
10. Navigate to payment section
11. Select payment method using `Tab` + `Space`
12. Press `Enter` on "Complete Sale" button

**Expected Results:**
- ✅ Search results keyboard navigable
- ✅ Quantity field supports keyboard input
- ✅ Cart items have keyboard-accessible delete buttons
- ✅ Payment method selection works with Space key
- ✅ Complete Sale activates with Enter

**Pass/Fail:** ________  **Notes:** ________________

---

#### 4. Inventory Screen (Product List)

**Steps:**
1. Navigate to Inventory screen
2. Use `Tab` to focus search field
3. Search for product
4. Press `Tab` to navigate table rows
5. Press `Enter` on first product row (should open edit dialog)
6. Use `Tab` to navigate form fields in dialog
7. Press `Esc` to close dialog
8. Verify focus returns to product row

**Expected Results:**
- ✅ Search field keyboard accessible
- ✅ Table rows focusable and activatable
- ✅ Dialog opens with Enter key
- ✅ Form fields in dialog keyboard navigable
- ✅ Esc closes dialog
- ✅ Focus restored after dialog close

**Pass/Fail:** ________  **Notes:** ________________

---

#### 5. Modal Dialog Navigation

**Steps:**
1. Open any modal dialog (e.g., Add Product)
2. Verify focus trapped within dialog (Tab doesn't escape)
3. Navigate all form fields with `Tab`
4. Press `Esc` to close dialog
5. Verify focus returns to trigger element

**Expected Results:**
- ✅ Focus trapped inside dialog (modal behavior)
- ✅ Tab cycles within dialog elements only
- ✅ Esc closes dialog
- ✅ Focus restored to element that opened dialog

**Pass/Fail:** ________  **Notes:** ________________

---

#### 6. Dropdown and Select Elements

**Steps:**
1. Navigate to Settings screen
2. Find dropdown (e.g., Currency selector)
3. Press `Tab` to focus dropdown
4. Press `Space` or `Enter` to open dropdown
5. Use `Arrow Up/Down` to navigate options
6. Press `Enter` to select option
7. Press `Esc` to close without selecting

**Expected Results:**
- ✅ Dropdown opens with Space/Enter
- ✅ Arrow keys navigate options
- ✅ Enter selects current option
- ✅ Esc closes dropdown

**Pass/Fail:** ________  **Notes:** ________________

---

### Keyboard Shortcuts Testing

Test any documented keyboard shortcuts (if implemented):

| Shortcut | Expected Action | Pass/Fail | Notes |
|----------|----------------|-----------|-------|
| Ctrl+N | New Sale | ________ | ______ |
| Ctrl+F | Focus Search | ________ | ______ |
| Ctrl+S | Save | ________ | ______ |
| Esc | Close Dialog | ________ | ______ |

---

### Keyboard Navigation Summary

**Total Tests:** 6  
**Passed:** ________  
**Failed:** ________  
**Blocked:** ________  

**Critical Issues Found:**
- [ ] Issue 1: ________________________
- [ ] Issue 2: ________________________

**Recommendation:** ☐ Pass  ☐ Fail  ☐ Pass with Minor Issues

---

## Screen Reader Testing

### Objective
Verify that all content and interactive elements are properly announced by screen readers for visually impaired users.

### Test Environment
- **Android:** TalkBack (Settings → Accessibility → TalkBack)
- **iOS:** VoiceOver (Settings → Accessibility → VoiceOver)
- **Success Criteria:** All elements have meaningful labels, navigation is logical, state changes announced

---

### TalkBack Testing (Android)

#### Setup TalkBack

1. Open **Settings** → **Accessibility** → **TalkBack**
2. Enable TalkBack
3. Complete TalkBack tutorial if first time
4. Launch Vibely POS app

#### Gestures

- **Swipe Right:** Next item
- **Swipe Left:** Previous item
- **Double Tap:** Activate item
- **Two Finger Swipe Down:** Read from top
- **Two Finger Swipe Up:** Read from current position

---

#### Test Scenarios (TalkBack)

**1. Login Screen**

**Steps:**
1. Launch app with TalkBack enabled
2. Swipe right to navigate through elements
3. Listen for announcements on each element

**Expected Announcements:**
- Email field: "Email, edit box, double tap to edit"
- Password field: "Password, edit box, double tap to edit"
- Show/Hide password button: "Show password, button, double tap to activate"
- Sign In button: "Sign In, button, double tap to activate"

**Pass/Fail:** ________  **Notes:** ________________

---

**2. Dashboard Metrics**

**Steps:**
1. Navigate to Dashboard after login
2. Swipe right through metric cards
3. Listen for announcements

**Expected Announcements:**
- "Today's Revenue, $1,234.56"
- "Transactions, 45"
- "Average Order Value, $27.43"
- "Top Product, Product Name"

**Pass/Fail:** ________  **Notes:** ________________

---

**3. Checkout Screen (Point of Sale)**

**Steps:**
1. Navigate to Checkout screen
2. Swipe through product search, cart items, payment buttons
3. Verify all interactive elements announced

**Expected Announcements:**
- "Search products, edit box"
- "Product Name, $9.99, Quantity: 2, button, double tap for options"
- "Remove item, button"
- "Cash, radio button, selected"
- "Complete Sale, button, double tap to activate"

**Pass/Fail:** ________  **Notes:** ________________

---

**4. Icon Buttons**

**Steps:**
1. Navigate to any screen with icon buttons (e.g., Inventory)
2. Focus on icon buttons
3. Verify meaningful labels announced

**Expected Announcements (Examples):**
- "Add product, button"
- "Edit, button"
- "Delete, button"
- "Search, button"
- "Filter, button"

**Pass/Fail:** ________  **Notes:** ________________

---

**5. State Changes and Feedback**

**Steps:**
1. Add product to cart in Checkout
2. Listen for confirmation announcement
3. Submit form with errors
4. Listen for error announcement

**Expected Announcements:**
- After adding to cart: "Product added to cart"
- On form error: "Error, Email is required"
- On success: "Product saved successfully"

**Pass/Fail:** ________  **Notes:** ________________

---

### VoiceOver Testing (iOS)

#### Setup VoiceOver

1. Open **Settings** → **Accessibility** → **VoiceOver**
2. Enable VoiceOver
3. Complete VoiceOver tutorial if first time
4. Launch Vibely POS app

#### Gestures

- **Swipe Right:** Next item
- **Swipe Left:** Previous item
- **Double Tap:** Activate item
- **Two Finger Swipe Down:** Read all
- **Three Finger Swipe Left/Right:** Navigate pages

---

#### Test Scenarios (VoiceOver)

**Perform the same test scenarios as TalkBack (1-5 above)**

**Expected Results:**
- ✅ All interactive elements announced with role (button, text field, etc.)
- ✅ Content descriptions present on all icons and images
- ✅ State changes announced (loading, error, success)
- ✅ Navigation is logical and predictable

**Pass/Fail:** ________  **Notes:** ________________

---

### Screen Reader Testing Summary

**TalkBack Tests Passed:** _____ / 5  
**VoiceOver Tests Passed:** _____ / 5  

**Critical Issues Found:**
- [ ] Missing content descriptions: ________________________
- [ ] Confusing announcements: ________________________
- [ ] State changes not announced: ________________________

**Recommendation:** ☐ Pass  ☐ Fail  ☐ Pass with Minor Issues

---

## Cross-Platform Testing

### Objective
Verify that the application functions correctly across all supported platforms: Desktop (Windows, macOS, Linux), Android, iOS, and Web.

### Test Matrix

| Feature | Windows | macOS | Linux | Android | iOS | Web | Notes |
|---------|---------|-------|-------|---------|-----|-----|-------|
| Login | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Dashboard | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Checkout | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Inventory | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Sales History | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Customers | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Suppliers | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Purchase Orders | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Shifts | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Reports | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| Settings | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |
| User Management | ☐ | ☐ | ☐ | ☐ | ☐ | ☐ | |

---

### Desktop Testing (Windows, macOS, Linux)

#### Build Applications

**Windows:**
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
# Installer: composeApp/build/compose/binaries/main/msi/
```

**macOS:**
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
# Installer: composeApp/build/compose/binaries/main/dmg/
```

**Linux (DEB):**
```bash
./gradlew :composeApp:packageDeb
# Installer: composeApp/build/compose/binaries/main/deb/
```

#### Test Checklist (Each Platform)

**Installation:**
- [ ] Installer launches without errors
- [ ] Application installs to correct location
- [ ] Desktop shortcut/launcher created
- [ ] Application appears in Start Menu/Applications

**Functionality:**
- [ ] Application launches successfully
- [ ] Login authentication works
- [ ] All screens load without errors
- [ ] CRUD operations work (Create, Read, Update, Delete)
- [ ] Search and filtering functional
- [ ] Pagination works correctly
- [ ] Forms validate properly
- [ ] Error handling displays correctly
- [ ] Offline mode detection works

**Performance:**
- [ ] UI renders smoothly (no lag)
- [ ] Large lists (500+ items) perform well
- [ ] Skeleton loading screens appear correctly
- [ ] No memory leaks (check Task Manager/Activity Monitor)

**UI/UX:**
- [ ] Layout renders correctly (no overflow/cutoff)
- [ ] Fonts and colors correct
- [ ] Icons display properly
- [ ] Modals and dialogs centered
- [ ] Navigation menu functional

---

### Mobile Testing (Android)

#### Build Android APK

```bash
./gradlew :composeApp:assembleDebug
# APK: composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

#### Install on Device

```bash
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

#### Test Checklist

**Installation:**
- [ ] APK installs without errors
- [ ] App icon appears on home screen
- [ ] Permissions requested (if any)

**Functionality:**
- [ ] All features work as on desktop
- [ ] Touch gestures responsive
- [ ] Virtual keyboard appears correctly
- [ ] Network requests succeed
- [ ] TalkBack compatibility verified (see Screen Reader Testing)

**Performance:**
- [ ] Smooth scrolling on lists
- [ ] No ANR (Application Not Responding) errors
- [ ] Battery usage acceptable

**Device Testing:**
- [ ] Test on API 24 (Android 7.0)
- [ ] Test on API 34 (Android 14)
- [ ] Test on different screen sizes (phone, tablet)

---

### Mobile Testing (iOS)

#### Build iOS App

```bash
./gradlew :composeApp:iosArm64
# Framework: composeApp/build/bin/iosArm64/
```

#### Install on Device

Use Xcode to build and install on physical device or simulator.

#### Test Checklist

**Installation:**
- [ ] App installs via Xcode
- [ ] App icon displays correctly
- [ ] Launch screen appears

**Functionality:**
- [ ] All features work as on desktop
- [ ] Touch gestures responsive
- [ ] Native iOS keyboard works
- [ ] Network requests succeed
- [ ] VoiceOver compatibility verified (see Screen Reader Testing)

**Performance:**
- [ ] Smooth animations and transitions
- [ ] No crashes or freezes
- [ ] Memory usage acceptable

**Device Testing:**
- [ ] Test on iOS 15
- [ ] Test on latest iOS version
- [ ] Test on different devices (iPhone, iPad)

---

### Web Testing

#### Build Web Application

```bash
# WebAssembly
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# JavaScript
./gradlew :composeApp:jsBrowserDevelopmentRun
```

#### Browser Testing Matrix

| Feature | Chrome | Firefox | Safari | Edge | Notes |
|---------|--------|---------|--------|------|-------|
| Login | ☐ | ☐ | ☐ | ☐ | |
| Dashboard | ☐ | ☐ | ☐ | ☐ | |
| Checkout | ☐ | ☐ | ☐ | ☐ | |
| All Screens | ☐ | ☐ | ☐ | ☐ | |

#### Test Checklist (Each Browser)

**Functionality:**
- [ ] Application loads without errors
- [ ] All features work as on desktop
- [ ] Browser console shows no errors
- [ ] Network requests succeed (check DevTools Network tab)

**Responsive Design:**
- [ ] Test at 1920x1080 (Desktop)
- [ ] Test at 1366x768 (Laptop)
- [ ] Test at 768x1024 (Tablet)
- [ ] Test at 375x667 (Mobile)

**Offline Mode:**
- [ ] Enable offline mode in DevTools
- [ ] Verify offline banner appears
- [ ] Verify online banner dismisses on reconnect

---

### Cross-Platform Testing Summary

**Platforms Tested:** _____ / 7  
**Critical Issues Found:**
- [ ] Platform: __________ Issue: __________________________
- [ ] Platform: __________ Issue: __________________________

**Recommendation:** ☐ Pass  ☐ Fail  ☐ Pass with Minor Issues

---

## Load Testing

### Objective
Verify that the backend API can handle 100 concurrent users without performance degradation, errors, or crashes.

### Test Environment
- **Tool:** Apache JMeter 5.5+ or Gatling 3.9+
- **Target:** Backend API (`http://localhost:8080/api/`)
- **Concurrent Users:** 100
- **Test Duration:** 10 minutes
- **Success Criteria:** 
  - 95th percentile response time < 500ms
  - Error rate < 1%
  - No server crashes or memory leaks

---

### Apache JMeter Setup

#### Install JMeter

```bash
# Download from https://jmeter.apache.org/download_jmeter.cgi
wget https://downloads.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xf apache-jmeter-5.6.3.tgz
cd apache-jmeter-5.6.3/bin
./jmeter
```

#### Create Test Plan

**Test Plan Structure:**

1. **Thread Group** - Simulates 100 users
   - Number of Threads: 100
   - Ramp-Up Period: 60 seconds (gradual increase)
   - Loop Count: 10 (each user performs 10 iterations)

2. **HTTP Request Defaults**
   - Server: `localhost`
   - Port: `8080`
   - Protocol: `http`

3. **User Authentication**
   - HTTP Request: `POST /api/auth/login`
   - Body: `{"email": "test@vibely.pos", "password": "Test123!"}`
   - Extract JWT token using JSON Extractor

4. **Test Scenarios** (Add multiple HTTP Requests)

**Scenario 1: Dashboard Load**
- GET `/api/dashboard/summary`
- Headers: `Authorization: Bearer ${token}`

**Scenario 2: Product List**
- GET `/api/products?page=1&page_size=50`
- Headers: `Authorization: Bearer ${token}`

**Scenario 3: Create Sale**
- POST `/api/sales`
- Headers: `Authorization: Bearer ${token}`
- Body: Sale data JSON

**Scenario 4: View Reports**
- POST `/api/reports/sales`
- Headers: `Authorization: Bearer ${token}`
- Body: `{"startDate": "2026-03-01", "endDate": "2026-03-16"}`

5. **Listeners** (Add to view results)
   - View Results Tree
   - Summary Report
   - Aggregate Report
   - Response Time Graph

#### Run Load Test

```bash
# Run in GUI mode
./jmeter.sh -t vibely-pos-load-test.jmx

# Run in CLI mode (recommended for accurate results)
./jmeter.sh -n -t vibely-pos-load-test.jmx -l results.jtl -e -o report/
```

---

### Gatling Setup (Alternative)

#### Install Gatling

```bash
# Download from https://gatling.io/open-source/
wget https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.9.5/gatling-charts-highcharts-bundle-3.9.5.zip
unzip gatling-charts-highcharts-bundle-3.9.5.zip
cd gatling-charts-highcharts-bundle-3.9.5
```

#### Create Test Simulation (Scala)

Create `user-files/simulations/VibelyPosSimulation.scala`:

```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class VibelyPosSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080/api")
    .acceptHeader("application/json")

  val scn = scenario("Vibely POS Load Test")
    .exec(
      http("Login")
        .post("/auth/login")
        .body(StringBody("""{"email":"test@vibely.pos","password":"Test123!"}""")).asJson
        .check(jsonPath("$.accessToken").saveAs("token"))
    )
    .pause(1)
    .exec(
      http("Dashboard")
        .get("/dashboard/summary")
        .header("Authorization", "Bearer ${token}")
    )
    .pause(2)
    .exec(
      http("Products")
        .get("/products?page=1&page_size=50")
        .header("Authorization", "Bearer ${token}")
    )
    .pause(2)
    .exec(
      http("Sales Report")
        .post("/reports/sales")
        .header("Authorization", "Bearer ${token}")
        .body(StringBody("""{"startDate":"2026-03-01","endDate":"2026-03-16"}""")).asJson
    )

  setUp(
    scn.inject(rampUsers(100).during(60.seconds))
  ).protocols(httpProtocol)
}
```

#### Run Load Test

```bash
./bin/gatling.sh
# Select simulation number for VibelyPosSimulation
```

---

### Performance Metrics to Monitor

**Backend Server Metrics:**
- CPU usage (should stay < 80%)
- Memory usage (watch for memory leaks)
- Response times (95th percentile < 500ms)
- Error rates (< 1%)
- Database connection pool usage

**Monitor Commands:**

```bash
# CPU and memory
top -p $(pgrep -f backend)

# JVM metrics (if using VisualVM or JConsole)
jvisualvm

# Database connections (Supabase dashboard)
# Check connection pooling metrics

# API response times
# Use JMeter/Gatling reports
```

---

### Load Test Acceptance Criteria

| Metric | Target | Actual | Pass/Fail |
|--------|--------|--------|-----------|
| Concurrent Users | 100 | ______ | ________ |
| Test Duration | 10 min | ______ | ________ |
| Avg Response Time | < 200ms | ______ | ________ |
| 95th Percentile | < 500ms | ______ | ________ |
| Max Response Time | < 2000ms | ______ | ________ |
| Error Rate | < 1% | ______ | ________ |
| Throughput | > 500 req/s | ______ | ________ |
| CPU Usage | < 80% | ______ | ________ |
| Memory Usage | Stable | ______ | ________ |

---

### Load Testing Summary

**Test Passed:** ☐ Yes  ☐ No  
**Performance Bottlenecks Identified:**
- [ ] Issue 1: ________________________
- [ ] Issue 2: ________________________

**Recommendations:**
- Increase server resources if CPU > 80%
- Optimize database queries if response times high
- Implement caching if same data requested frequently
- Add connection pooling if database connections exhausted

---

## Security Penetration Testing

### Objective
Identify security vulnerabilities in the Vibely POS application and backend API, focusing on OWASP Top 10 risks.

### Test Environment
- **Tool:** OWASP ZAP 2.12+ or Burp Suite Community
- **Target:** Backend API (`http://localhost:8080/api/`)
- **Success Criteria:** No critical or high-severity vulnerabilities

---

### OWASP Top 10 (2021) Test Cases

| # | Vulnerability | Test | Pass/Fail | Notes |
|---|--------------|------|-----------|-------|
| 1 | Broken Access Control | Unauthorized endpoint access | ________ | ______ |
| 2 | Cryptographic Failures | Sensitive data exposure | ________ | ______ |
| 3 | Injection | SQL/Command injection | ________ | ______ |
| 4 | Insecure Design | Business logic flaws | ________ | ______ |
| 5 | Security Misconfiguration | Default configs, verbose errors | ________ | ______ |
| 6 | Vulnerable Components | Outdated dependencies | ________ | ______ |
| 7 | Auth Failures | Weak passwords, session handling | ________ | ______ |
| 8 | Data Integrity | Unsigned/unverified data | ________ | ______ |
| 9 | Logging Failures | Insufficient logging | ________ | ______ |
| 10 | SSRF | Server-side request forgery | ________ | ______ |

---

### OWASP ZAP Setup

#### Install OWASP ZAP

```bash
# Download from https://www.zaproxy.org/download/
wget https://github.com/zaproxy/zaproxy/releases/download/v2.14.0/ZAP_2.14.0_Linux.tar.gz
tar -xf ZAP_2.14.0_Linux.tar.gz
cd ZAP_2.14.0
./zap.sh
```

#### Configure ZAP

1. **Set Target:** `http://localhost:8080`
2. **Configure Authentication:**
   - Manual authentication via ZAP browser
   - Login at `/api/auth/login`
   - ZAP will capture JWT token
3. **Include in Context:** `/api/*`
4. **Exclude from Context:** Static files, health checks

---

### Automated Scan

**Steps:**
1. Click **Automated Scan** button
2. Enter URL: `http://localhost:8080/api`
3. Select **Use traditional spider**
4. Check **Use ajax spider**
5. Click **Attack**
6. Wait for scan completion (10-30 minutes)

**Review Results:**
- High severity: Red flags (requires immediate fix)
- Medium severity: Orange flags (should fix)
- Low severity: Yellow flags (nice to fix)

---

### Manual Security Tests

#### 1. Broken Access Control

**Test: Access admin endpoints as cashier**

```bash
# Login as cashier
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cashier@vibely.pos","password":"Cashier123!"}'

# Copy accessToken from response
export CASHIER_TOKEN="<access_token>"

# Attempt to access admin-only endpoint
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $CASHIER_TOKEN"

# Expected: 403 Forbidden
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

**Test: Manipulate user IDs in URLs**

```bash
# Login as user ID 1
# Attempt to access user ID 2's data
curl -X GET http://localhost:8080/api/users/2 \
  -H "Authorization: Bearer $TOKEN"

# Expected: 403 Forbidden (if not authorized)
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 2. SQL Injection

**Test: Inject SQL in search parameters**

```bash
# Attempt SQL injection in product search
curl -X GET "http://localhost:8080/api/products?search=' OR '1'='1" \
  -H "Authorization: Bearer $TOKEN"

# Expected: Escaped/sanitized query, no database errors
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

**Test: Inject SQL in POST body**

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test'; DROP TABLE customers; --","email":"test@test.com"}'

# Expected: Validation error or escaped characters
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 3. Cross-Site Scripting (XSS)

**Test: Inject JavaScript in text fields**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"<script>alert(1)</script>","price":10}'

# Expected: Escaped characters in response, no script execution
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 4. Authentication Bypass

**Test: Access endpoints without token**

```bash
curl -X GET http://localhost:8080/api/dashboard/summary

# Expected: 401 Unauthorized
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

**Test: Use expired/invalid token**

```bash
curl -X GET http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer invalid.token.here"

# Expected: 401 Unauthorized
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 5. Rate Limiting

**Test: Exceed rate limit**

```bash
# Send 10 requests rapidly to login endpoint
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}'
done

# Expected: HTTP 429 (Too Many Requests) after 5 attempts
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 6. CSRF Protection

**Test: Submit state-changing request without CSRF token**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":10}'
  # NOTE: Should include CSRF token in headers

# Expected: 403 Forbidden (if CSRF protection enabled)
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 7. Security Headers

**Test: Check for security headers**

```bash
curl -I http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"

# Check for presence of:
# - Content-Security-Policy: ________________
# - X-Frame-Options: ________________
# - X-Content-Type-Options: ________________
# - Strict-Transport-Security: ________________
# - X-XSS-Protection: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 8. Sensitive Data Exposure

**Test: Check for sensitive data in responses**

```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"

# Verify passwords are NOT returned in response
# Verify tokens are NOT exposed
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

#### 9. Insecure Direct Object References (IDOR)

**Test: Access resources by ID manipulation**

```bash
# Create sale as user 1
# Attempt to view sale created by user 2
curl -X GET http://localhost:8080/api/sales/123 \
  -H "Authorization: Bearer $USER1_TOKEN"

# Expected: 403 Forbidden (if sale belongs to user 2 and user 1 is not authorized)
# Actual: ________________
```

**Pass/Fail:** ________  **Notes:** ________________

---

### Security Testing Summary

**Total Tests:** 15+  
**Passed:** ________  
**Failed:** ________  
**Critical Issues:** ________  
**High Issues:** ________  
**Medium Issues:** ________  
**Low Issues:** ________  

**Critical/High Vulnerabilities Found:**
- [ ] Vulnerability 1: ________________________
- [ ] Vulnerability 2: ________________________

**Recommendation:** ☐ Pass  ☐ Fail  ☐ Pass with Fixes Required

---

## Test Reporting

### Test Summary Report Template

```markdown
# Vibely POS - Test Summary Report

**Date:** ____________
**Tester:** ____________
**Version:** 1.0.0

## Executive Summary

Total tests executed: ________
Passed: ________
Failed: ________
Blocked: ________

## Test Results by Category

### Keyboard Navigation (Desktop)
- Status: ☐ Pass ☐ Fail
- Critical Issues: ________

### Screen Reader Compatibility
- TalkBack: ☐ Pass ☐ Fail
- VoiceOver: ☐ Pass ☐ Fail
- Critical Issues: ________

### Cross-Platform Testing
- Windows: ☐ Pass ☐ Fail
- macOS: ☐ Pass ☐ Fail
- Linux: ☐ Pass ☐ Fail
- Android: ☐ Pass ☐ Fail
- iOS: ☐ Pass ☐ Fail
- Web: ☐ Pass ☐ Fail
- Critical Issues: ________

### Load Testing
- 100 Concurrent Users: ☐ Pass ☐ Fail
- Response Time: ☐ Pass ☐ Fail
- Error Rate: ☐ Pass ☐ Fail
- Critical Issues: ________

### Security Testing
- OWASP Top 10: ☐ Pass ☐ Fail
- Critical Vulnerabilities: ________
- High Vulnerabilities: ________

## Critical Issues

1. **[CRITICAL] Issue Title**
   - Description: ________________
   - Steps to Reproduce: ________________
   - Expected: ________________
   - Actual: ________________
   - Impact: High/Critical
   - Priority: P0

## Recommendations

- [ ] Fix critical issues before production
- [ ] Address high-priority accessibility issues
- [ ] Optimize performance bottlenecks
- [ ] Patch security vulnerabilities

## Sign-Off

☐ Approved for Production  
☐ Approved with Minor Issues  
☐ Not Approved - Critical Issues Found

Tester Signature: ________________  
Date: ________________
```

---

## Appendix: Useful Commands

### Backend Health Check

```bash
curl http://localhost:8080/health
```

### Database Connection Test

```bash
curl http://localhost:8080/api/test/database
```

### Generate Test JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@vibely.pos","password":"Admin123!"}'
```

### Monitor Backend Logs

```bash
tail -f backend/logs/application.log
```

### Check Backend Process

```bash
ps aux | grep backend
```

---

**End of Testing Guide**

For questions or issues, contact the development team at dev@vibely.pos
