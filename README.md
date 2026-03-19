# 🎯 Retirement Planner — Android App

A financial planning app to calculate the monthly SIP contribution needed to reach your retirement corpus target, and plan your monthly expenses at retirement.

---

## 📸 Screenshots

<p float="left">
  <img src="app/src/main/res/drawable/screenshot1.jpeg" width="23%" />
  <img src="app/src/main/res/drawable/screenshot2.jpeg" width="23%" />
  <img src="app/src/main/res/drawable/screenshot3.jpeg" width="23%" />
  <img src="app/src/main/res/drawable/screenshot4.jpeg" width="23%" />
</p>

---

## 📱 Features

### 💼 Portfolio Planner
- **Fully dynamic asset list** — add, remove, and rename any investment (NPS, PPF, EPF, MF, Stocks, Crypto, Savings Bank, LIC, or anything you want)
- **User-defined growth rates** — set your own expected return % against each asset individually
- **HUF Portfolio** — separate fixed section for HUF Bank, HUF Stocks, and HUF MF
- **Projected growth table** — each asset shown separately with its own rate, current value, and projected value at retirement. No merging, no assumptions
- **Gap analysis** — exact corpus shortfall and monthly SIP required (always calculated at 12% equity rate)
- **Alt. Debt SIP** — equivalent monthly SIP if investing in debt instruments at 8%

### 🔴 Liabilities / Debt Section
- Track Home Loan, Car Loan, Credit Cards, or any custom liability
- Amounts are always stored as negative values — the app auto-converts any positive number you type
- Add or remove entries freely

### 🏠 Properties Section
- Add any number of properties with editable names and current market values
- Tracks real estate and other physical assets separately

### 💰 Net Worth Summary
- Real-time net worth = Investments + Properties − Liabilities
- Updates instantly as you type any value
- Color coded — green when positive, red when negative

### 📸 Net Worth History
- Tap **Save Net Worth Snapshot** to record your net worth at any point in time
- History screen shows all past snapshots newest-first, each with a trend badge (▲ increase / ▼ decrease vs previous)
- Summary shows total snapshots, first recorded date, and overall change since you started
- Snapshots are manual — you control when to save, so history only reflects complete and intentional data

### 💸 Expense Planner Tab
- **15 pre-filled current expenses** — Groceries, Utilities, EMI, Insurance, Dining etc.
- Uncheck expenses that won't exist at retirement (e.g. Home Loan EMI, Child Education)
- **Post-retirement expenses** — add new expenses that appear only in old age (Medical, Caretaker, Travel)
- All amounts entered in today's value
- **Real-time totals** — both sections update instantly as you type or check/uncheck
- Add or remove expense rows freely with the ✕ delete button

### 📊 Retirement Expense Calculation
- Inflation-adjusts your combined expenses to retirement date
- Calculates the **minimum corpus needed at retirement** to never run out of money
- Uses Present Value of Growing Annuity formula — accounts for both investment return and inflation during retirement
- Configurable: retirement age, life expectancy, inflation rate, post-retirement return on corpus

### 💾 Backup & Restore
- **Export** — saves all your data as a JSON file. Use Android's file picker to save directly to Google Drive, Downloads, or anywhere
- **Restore** — pick a previously exported JSON file to restore everything including net worth history
- Backup includes portfolio data, expense data, and full net worth history
- App automatically restarts after a successful restore so all screens reload immediately

### 🔄 Auto-Save & Data Persistence
- All data is saved automatically every time you tap Calculate or make changes
- App shows when data was last saved with a green banner at the top
- On restart, all fields are pre-filled exactly as you left them
- Clear button with confirmation dialog to wipe all saved data

### 🌙 Dark Mode Support
- Fully supports Android light and dark system themes
- All text, backgrounds, and cards adapt automatically — no invisible text in dark mode

### ℹ️ About Page
- Creator info with direct email button
- Bitcoin donation address with one-tap copy

---

## 📊 Default Rate Assumptions

| Asset Class        | Default Rate |
|--------------------|-------------|
| NPS                | 10% p.a.    |
| PPF / EPF          | 8% p.a.     |
| Mutual Funds       | 12% p.a.    |
| Stocks             | 12% p.a.    |
| HUF Stocks / MF    | 12% p.a.    |
| HUF Bank           | 4% p.a.     |
| Savings Bank       | 4% p.a.     |
| Crypto / Bitcoin   | 20% p.a.    |
| Inflation          | 6% p.a.     |
| Monthly SIP rate   | 12% p.a. (fixed) |

All rates are editable — these are just the defaults shown in gray when you first open the app.

---

## 📲 Install on Your Phone

The APK is ready to install — no need to build anything.

1. **Download the APK** directly from the link below:

   👉 [Download RetirementPlanner.apk](https://github.com/yogesh249/RetirementPlanner/raw/main/RetirementPlanner.apk)

2. **Allow unknown sources** on your Android phone:
   - Go to `Settings → Security` (or `Settings → Apps → Special app access`)
   - Enable **"Install unknown apps"** or **"Unknown sources"**
   - On newer Android (10+): you may be prompted automatically when you tap the APK — just tap **"Allow"**

3. **Open the APK file** on your phone:
   - If you downloaded on your phone — tap the notification or find it in your Downloads folder
   - If you downloaded on PC — transfer it via USB, WhatsApp, Google Drive, or email
   - Tap the file to install

4. **Tap Install** and you're done. 🎉

> ⚠️ Android may warn "App from unknown source" — this is normal for apps not on the Play Store. The app is completely safe, open source, and the full source code is in this repository.

---

## 🔨 How to Build the APK

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (free, ~1GB download)
- ~10 minutes of setup time

### Steps

1. **Download and install Android Studio** from https://developer.android.com/studio

2. **Open this project**:
   - Launch Android Studio
   - Click `File → Open`
   - Select the `RetirementPlanner` folder (this folder)
   - Click OK

3. **Wait for Gradle sync** (~2-3 minutes on first run — it downloads dependencies automatically)

4. **Build the APK**:
   - Click `Build → Build Bundle(s) / APK(s) → Build APK(s)`
   - Wait ~1-2 minutes

5. **Find your APK**:
   - A notification appears: "APK(s) generated successfully"
   - Click `locate` in the notification
   - APK is at: `app/build/outputs/apk/debug/app-debug.apk`

6. **Install on your phone**:
   - Transfer the APK to your Android phone (USB, WhatsApp, email, Google Drive)
   - On your phone: `Settings → Security → Allow unknown sources` (or "Install unknown apps")
   - Tap the APK file to install

---

## 📐 Financial Logic

### Portfolio Projection
Each asset grows independently using compound interest with its own rate:
```
Future Value = Present Value × (1 + rate)^years
```

### Gap Calculation
```
Gap = Target Corpus − Total Projected Portfolio
```

### Monthly SIP (FV of Annuity)
```
Monthly SIP = Gap × (r/12) / [(1 + r/12)^months − 1]
```
Where r = 12% p.a. (fixed equity rate — independent of your asset rates)

### Minimum Corpus at Retirement (Present Value of Growing Annuity)
```
Min Corpus = PMT × [1 − ((1+g)/(1+r))^n] / (r − g)
```
Where PMT = inflation-adjusted monthly expense, r = post-retirement return, g = inflation rate, n = retirement months

---

## 🗂 Project Structure

```
RetirementPlanner/
├── app/
│   ├── src/main/
│   │   ├── java/com/retirement/planner/
│   │   │   ├── MainActivity.java              ← Tab host (ViewPager2)
│   │   │   ├── PortfolioFragment.java         ← Portfolio planner tab
│   │   │   ├── ExpenseFragment.java           ← Expense planner tab
│   │   │   ├── ResultActivity.java            ← Results & calculations
│   │   │   ├── NetWorthHistoryActivity.java   ← Net worth history screen
│   │   │   ├── BackupRestoreActivity.java     ← Backup & restore screen
│   │   │   ├── DefaultValueHelper.java        ← Gray hint / default value utility
│   │   │   └── AboutActivity.java             ← About & Bitcoin donation
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml              ← Tab host layout
│   │   │   │   ├── fragment_portfolio.xml         ← Portfolio tab UI
│   │   │   │   ├── fragment_expense.xml           ← Expense tab UI
│   │   │   │   ├── activity_result.xml            ← Results UI
│   │   │   │   ├── activity_about.xml             ← About UI
│   │   │   │   ├── activity_networth_history.xml  ← History screen UI
│   │   │   │   ├── activity_backup.xml            ← Backup screen UI
│   │   │   │   ├── portfolio_asset_row.xml        ← Dynamic asset row
│   │   │   │   ├── debt_row.xml                   ← Debt/liability row
│   │   │   │   ├── property_row.xml               ← Property row
│   │   │   │   ├── expense_row_current.xml        ← Current expense row
│   │   │   │   ├── expense_row_post.xml           ← Post-retirement expense row
│   │   │   │   ├── item_networth_history.xml      ← History item card
│   │   │   │   └── table_row_breakdown.xml        ← Results table row
│   │   │   ├── drawable/        ← Backgrounds, styles & screenshots
│   │   │   ├── color/           ← Color selectors (tab text)
│   │   │   ├── values/          ← Colors, strings, themes (light mode)
│   │   │   └── values-night/    ← Dark mode color overrides
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🆘 Troubleshooting

**"Gradle sync failed"** → Check your internet connection; Gradle needs to download dependencies (~50MB)

**"SDK not found"** → In Android Studio: `Tools → SDK Manager` → Install Android SDK 34

**"Cannot install APK"** → Enable "Unknown sources" in phone Settings → Security

**"Values showing in gray after restore"** → Create a fresh backup after installing the latest version — old backups from earlier versions may have incomplete data

---

Built with ❤️ for Indian retirement planning
