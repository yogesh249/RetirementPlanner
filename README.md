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

- **10 Asset Inputs**: NPS, PPF, EPF, MF, Stocks, Savings Bank, Crypto, HUF Bank, HUF Stocks, HUF MF
- **User-defined growth rates** — set your own expected return % for each asset
- **Projected growth** per asset class with full breakdown table
- **Gap analysis**: Shows exactly how much monthly SIP you still need
- **Expense Planner tab** — plan your monthly expenses at retirement
- **Minimum corpus calculator** — the exact corpus needed to never run out of money
- **Life expectancy & post-retirement return** inputs for accurate planning
- **Pre-filled common expenses** with checkbox to mark what continues at retirement
- **Real-time totals** — updates as you type
- **Auto-save** — all data saved on your phone, prefilled on next launch
- **Configurable retirement age** (default: 60)
- **Indian currency formatting** (Lakhs / Crores)
- **About page** with creator info and Bitcoin donation address

---

## 📊 Rate Assumptions

| Asset Class        | Growth Rate |
|--------------------|-------------|
| NPS                | 10% p.a.    |
| PPF / EPF          | 8% p.a.     |
| Mutual Funds       | 12% p.a.    |
| Stocks             | 12% p.a.    |
| HUF Stocks/MF      | 12% p.a.    |
| HUF Bank           | 4% p.a.     |
| Inflation          | 6% p.a.     |

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

### Step 1: Project Current Portfolio
Each asset grows using compound interest:
```
Future Value = Present Value × (1 + rate)^years
```

### Step 2: Inflation-Adjust the Target
```
Adjusted Target = Target × (1 + 0.06)^years
```

### Step 3: Calculate Gap
```
Gap = Adjusted Target − Total Projected Portfolio
```

### Step 4: Calculate Monthly SIP (FV of Annuity)
```
Monthly SIP = Gap × (r/12) / [(1 + r/12)^months − 1]
```
Where r = 12% (equity growth rate)

---

## 🗂 Project Structure

```
RetirementPlanner/
├── app/
│   ├── src/main/
│   │   ├── java/com/retirement/planner/
│   │   │   ├── MainActivity.java         ← Tab host (ViewPager2)
│   │   │   ├── PortfolioFragment.java    ← Portfolio planner tab
│   │   │   ├── ExpenseFragment.java      ← Expense planner tab
│   │   │   ├── ResultActivity.java       ← Results & calculations
│   │   │   └── AboutActivity.java        ← About & Bitcoin donation
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml        ← Tab host layout
│   │   │   │   ├── fragment_portfolio.xml   ← Portfolio tab UI
│   │   │   │   ├── fragment_expense.xml     ← Expense tab UI
│   │   │   │   ├── activity_result.xml      ← Results UI
│   │   │   │   ├── activity_about.xml       ← About UI
│   │   │   │   ├── asset_row.xml            ← Reusable asset input row
│   │   │   │   ├── expense_row_current.xml  ← Current expense row
│   │   │   │   ├── expense_row_post.xml     ← Post-retirement expense row
│   │   │   │   └── table_row_breakdown.xml  ← Results table row
│   │   │   ├── drawable/               ← Backgrounds, styles & screenshots
│   │   │   ├── color/                  ← Color selectors (tab text)
│   │   │   └── values/                 ← Colors, strings, themes
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

---

Built with ❤️ for Indian retirement planning