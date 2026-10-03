package com.tradinggate.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/*
 * TRADING GATE - V3 UI
 * Design master:
 * - dark premium / soft dusty pink
 * - Home / Checklist / Riwayat / Pengaturan
 * - 5 Gate accesses per WITA day
 * - daily reset at 08:00 WITA
 * - each Gate session = 15 minutes
 * - Liquidity + CISD mandatory
 * - BOS + IDM optional
 * - Entry Plan uses tap choices
 *
 * MT5 SL auto-detection is intentionally NOT connected yet.
 * The SL counter remains a placeholder until the MT5/VPS layer is added.
 */
public class MainActivity extends Activity {

    static final String MT5 = "net.metaquotes.metatrader5";
    static final String PREF = "trading_gate_v3";
    static final String TZ = "Asia/Makassar";

    static final long SESSION_MS = 15 * 60 * 1000L;
    static final int MAX_ACCESS = 5;
    static final int MAX_SL = 3;

    // ---------- COLORS ----------
    final int BG = Color.rgb(7, 8, 10);
    final int CARD = Color.rgb(16, 17, 20);
    final int CARD_2 = Color.rgb(23, 22, 27);
    final int BORDER = Color.rgb(59, 51, 61);
    final int PINK = Color.rgb(244, 113, 158);
    final int PINK_SOFT = Color.rgb(255, 174, 202);
    final int PINK_DARK = Color.rgb(83, 36, 57);
    final int WHITE = Color.rgb(248, 246, 248);
    final int MUTED = Color.rgb(153, 149, 157);
    final int GREEN = Color.rgb(135, 220, 169);
    final int RED = Color.rgb(242, 104, 133);

    SharedPreferences pref;

    LinearLayout root;
    FrameLayout content;
    LinearLayout bottomNav;

    TextView homeStatus, homeStatusSub, accessValue, slValue, resetValue;
    TextView checklistProgress, sessionTimer, sessionStatus;
    Button homeOpenButton, confirmButton, mt5Button;

    // Entry plan choices
    String direction = "";
    String zone = "";
    String timeframe = "";
    String rr = "";
    boolean liquidity = false;
    boolean cisd = false;
    boolean bos = false;
    boolean idm = false;
    boolean newsChecked = false;
    boolean conditionOk = false;

    CountDownTimer timer;
    boolean showingIntro = false;

    final DateTimeFormatter DATE_KEY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);

    final DateTimeFormatter DATE_DISPLAY =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.US);

    int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    int accessCount() {
        checkDailyReset();
        return pref.getInt("access_count", 0);
    }

    int slCount() {
        checkDailyReset();
        return pref.getInt("sl_count", 0);
    }

    String gateDayKey() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(TZ));
        LocalDate d = now.toLocalDate();

        // A trading day begins at 08:00 WITA.
        if (now.toLocalTime().isBefore(LocalTime.of(8, 0))) {
            d = d.minusDays(1);
        }
        return d.format(DATE_KEY);
    }

    void checkDailyReset() {
        String key = gateDayKey();
        String saved = pref.getString("gate_day", "");

        if (!key.equals(saved)) {
            pref.edit()
                    .putString("gate_day", key)
                    .putInt("access_count", 0)
                    .putInt("sl_count", 0)
                    .apply();
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        pref = getSharedPreferences(PREF, Context.MODE_PRIVATE);
        checkDailyReset();

        buildShell();

       
            showingIntro = true;
            showLanding();
    
    @Override
    protected void onResume() {
        super.onResume();
        if (pref != null) {
            checkDailyReset();
            if (!showingIntro) {
                if (isSessionActive()) {
                    showActiveSession();
                } else {
                    refreshHome();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    // =========================================================
    // BASIC UI
    // =========================================================

    TextView text(String value, float size) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(WHITE);
        v.setTextSize(size);
        return v;
    }

    GradientDrawable rounded(int fill, int stroke, int strokeWidth, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radius));
        if (strokeWidth > 0) {
            g.setStroke(dp(strokeWidth), stroke);
        }
        return g;
    }

    LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15), dp(14), dp(15), dp(14));
        c.setBackground(rounded(CARD, BORDER, 1, 18));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        c.setLayoutParams(lp);

        return c;
    }

    Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(13);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(8), 0, dp(8), 0);
        b.setBackground(rounded(CARD_2, BORDER, 1, 14));
        return b;
    }

    Button pinkButton(String label) {
        Button b = button(label);
        b.setText(label);
        b.setTextColor(Color.rgb(28, 18, 23));
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextSize(14);
        b.setBackground(rounded(PINK_SOFT, PINK, 0, 15));
        return b;
    }

    TextView title(String value) {
        TextView v = text(value, 16);
        v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setTextColor(PINK_SOFT);
        return v;
    }

    void section(LinearLayout c, String number, String heading, String sub) {
        TextView h = title(number + "  " + heading);
        c.addView(h);

        TextView s = text(sub, 11);
        s.setTextColor(MUTED);
        s.setPadding(0, dp(3), 0, dp(10));
        c.addView(s);
    }

    TextView centerText(String value, float size, int color) {
        TextView v = text(value, size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER);
        return v;
    }

    // =========================================================
    // SHELL / NAVIGATION
    // =========================================================

    void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        content = new FrameLayout(this);
        content.setBackgroundColor(BG);

        root.addView(content,
                new LinearLayout.LayoutParams(-1, 0, 1));

        buildBottomNav();

        root.addView(bottomNav,
                new LinearLayout.LayoutParams(-1, dp(64)));

        setContentView(root);
    }

    void buildBottomNav() {
        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setPadding(dp(5), dp(5), dp(5), dp(5));
        bottomNav.setBackgroundColor(Color.rgb(11, 11, 14));

        addNav("⌂\nHome", 0);
        addNav("☑\nChecklist", 1);
        addNav("▣\nRiwayat", 2);
        addNav("⚙\nPengaturan", 3);
    }

    void addNav(String label, int page) {
        TextView n = centerText(label, 10, MUTED);
        n.setGravity(Gravity.CENTER);
        n.setPadding(0, dp(4), 0, dp(2));
        n.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, -1, 1);

        bottomNav.addView(n, lp);

        n.setOnClickListener(v -> {
            if (page == 0) showHome();
            if (page == 1) showChecklist();
            if (page == 2) showHistory();
            if (page == 3) showSettings();
        });
    }

    void selectNav(int page) {
        if (bottomNav == null) return;
        for (int i = 0; i < bottomNav.getChildCount(); i++) {
            View v = bottomNav.getChildAt(i);
            if (v instanceof TextView) {
                TextView t = (TextView) v;
                t.setTextColor(i == page ? PINK_SOFT : MUTED);
                t.setTypeface(Typeface.DEFAULT, i == page
                        ? Typeface.BOLD : Typeface.NORMAL);
            }
        }
    }

    void hideBottomNav() {
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);
    }

    void showBottomNav() {
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
    }

    void clearContent() {
        content.removeAllViews();
    }

    ScrollView pageScroll() {
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setBackgroundColor(BG);
        return s;
    }

    LinearLayout pageRoot() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(16), dp(16), dp(16), dp(22));
        return p;
    }

    void pageHeader(LinearLayout p, String subtitle) {
        TextView brand = centerText("TRADING GATE", 20, WHITE);
brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
brand.setLetterSpacing(.08f);

LinearLayout.LayoutParams brandParams =
        new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
brandParams.topMargin = dp(14);
p.addView(brand, brandParams);

        TextView sub = centerText(subtitle, 9, PINK);
        sub.setLetterSpacing(.22f);
        sub.setPadding(0, dp(2), 0, dp(13));
        p.addView(sub);
    }

    TextView smallLabel(String value) {
        TextView v = text(value, 9);
        v.setTextColor(MUTED);
        v.setLetterSpacing(.08f);
        return v;
    }

    // =========================================================
    // HOME
    // =========================================================

    void showLanding() {
        showingIntro = true;
        clearContent();
        hideBottomNav();

        ScrollView scroll = pageScroll();
        LinearLayout p = pageRoot();
        p.setPadding(0, 0, 0, dp(18));

        GateHeroView hero = new GateHeroView(this);
        p.addView(hero, new LinearLayout.LayoutParams(-1, dp(560)));

        TextView brand = centerText("TRADING", 27, WHITE);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setLetterSpacing(.16f);
        p.addView(brand);

        TextView gate = centerText("GATE", 31, PINK_SOFT);
        gate.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        gate.setLetterSpacing(.12f);
        p.addView(gate);

        TextView sub = centerText("D I S C I P L I N E   S Y S T E M", 9, PINK_SOFT);
        sub.setPadding(0, dp(4), 0, dp(12));
        p.addView(sub);

        TextView motto = centerText("NO SETUP. NO ENTRY.", 10, WHITE);
        motto.setLetterSpacing(.18f);
        motto.setPadding(0, dp(6), 0, dp(16));
        p.addView(motto);

        Button start = pinkButton("MULAI   ›");
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, dp(54));
        lp.setMargins(dp(24), 0, dp(24), 0);
        p.addView(start, lp);

        start.setOnClickListener(v -> {
            pref.edit().putBoolean("intro_seen", true).apply();
            showingIntro = false;
            showHome();
        });

        scroll.addView(p);
        content.addView(scroll);
    }

    void showHome() {
        showingIntro = false;
        clearContent();
        showBottomNav();
        selectNav(0);

        ScrollView scroll = pageScroll();
        LinearLayout p = pageRoot();

        pageHeader(p, "DISCIPLINE SYSTEM");

        LinearLayout date = card();
        date.setPadding(dp(14), dp(11), dp(14), dp(11));
        TextView d = text(
                "▣   " + ZonedDateTime.now(ZoneId.of(TZ))
                        .format(DateTimeFormatter.ofPattern(
                                "EEEE, d MMM yyyy", Locale.US)),
                12);
        d.setTextColor(WHITE);
        date.addView(d);
        p.addView(date);

        LinearLayout statusCard = card();
        statusCard.setPadding(dp(15), dp(13), dp(15), dp(13));
        statusCard.setBackground(
                rounded(Color.rgb(29, 16, 24), Color.rgb(121, 56, 82), 1, 18));

        TextView sc = smallLabel("STATUS GATE");
        sc.setTextColor(PINK_SOFT);
        homeStatus = text("READY", 27);
        homeStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        homeStatus.setTextColor(PINK_SOFT);
        homeStatusSub = text("Siap untuk membuka Gate", 11);
        homeStatusSub.setTextColor(WHITE);

        statusCard.addView(sc);
        statusCard.addView(homeStatus);
        statusCard.addView(homeStatusSub);
        p.addView(statusCard);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout a = statCard("AKSES HARI INI",
                accessCount() + " / " + MAX_ACCESS);
        LinearLayout slc = statCard("SL HARI INI",
                slCount() + " / " + MAX_SL);

        stats.addView(a, halfParams(false));
        stats.addView(slc, halfParams(true));
        p.addView(stats);

        LinearLayout reset = card();
        TextView rt = smallLabel("◷  RESET HARIAN");
        rt.setTextColor(PINK);
        resetValue = text(nextResetText(), 12);
        resetValue.setTextColor(WHITE);
        reset.addView(rt);
        reset.addView(resetValue);
        p.addView(reset);

        homeOpenButton = pinkButton("BUKA GATE   ›");
        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(-1, dp(55));
        bp.setMargins(0, dp(2), 0, dp(9));
        p.addView(homeOpenButton, bp);
        homeOpenButton.setOnClickListener(v -> openGateFlow());

        if (isSessionActive()) {
            homeOpenButton.setText("SESI AKTIF   ›");
            homeOpenButton.setOnClickListener(v -> showActiveSession());
        } else if (accessCount() >= MAX_ACCESS || slCount() >= MAX_SL) {
            homeOpenButton.setText("GATE TERKUNCI HARI INI");
            homeOpenButton.setEnabled(false);
            homeOpenButton.setTextColor(MUTED);
            homeOpenButton.setBackground(rounded(CARD_2, BORDER, 1, 15));
        }

        TextView motto = centerText(
                "NO SETUP. NO ENTRY.", 9, MUTED);
        motto.setLetterSpacing(.16f);
        p.addView(motto);

        scroll.addView(p);
        content.addView(scroll);
    }

    String nextResetText() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(TZ));
        ZonedDateTime next = now.with(LocalTime.of(8, 0));

        if (!now.isBefore(next)) {
            next = next.plusDays(1);
        }

        return next.format(DateTimeFormatter.ofPattern(
                "EEEE, d MMM • HH:mm 'WITA'", Locale.US));
    }

    void refreshHome() {
        if (content.getChildCount() == 0) return;
        showHome();
    }

    // =========================================================
    // CHECKLIST
    // =========================================================

    void showChecklist() {
        clearContent();
        showBottomNav();
        selectNav(1);

        ScrollView scroll = pageScroll();
        LinearLayout p = pageRoot();

        pageHeader(p, "CHECKLIST");

        LinearLayout intro = card();
        checklistProgress = text(checklistCount() + " / 7 CHECKLIST", 18);
        checklistProgress.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        checklistProgress.setTextColor(PINK_SOFT);
        intro.addView(checklistProgress);

        TextView hint = text(
                "Lengkapi setup sebelum Gate boleh dibuka.",
                11);
        hint.setTextColor(MUTED);
        intro.addView(hint);
        p.addView(intro);

        addChecklistRow(p, "1", "Kondisi Diri",
                conditionOk ? "Fisik • mental • emosi ✓" : "Fisik • mental • emosi",
                conditionOk,
                v -> showCondition());

        addChecklistRow(p, "2", "Analisis HTF",
                (h4Ok() && h2Ok() && h1Ok())
                        ? "H4 / H2 / H1 ✓" : "H4 / H2 / H1",
                h4Ok() && h2Ok() && h1Ok(),
                v -> showHTF());

        addChecklistRow(p, "3", "News Check",
                newsChecked ? "Kalender ekonomi ✓" : "Kalender ekonomi",
                newsChecked,
                v -> showNews());

        addChecklistRow(p, "4", "Liquidity",
                liquidity ? "Area likuiditas ✓" : "Area likuiditas",
                liquidity,
                v -> {
                    liquidity = !liquidity;
                    showChecklist();
                });

        addChecklistRow(p, "5", "CISD",
                cisd ? "Konfirmasi struktur ✓" : "Konfirmasi struktur",
                cisd,
                v -> {
                    cisd = !cisd;
                    showChecklist();
                });

        addChecklistRow(p, "6", "Entry Plan",
                planComplete()
                        ? direction + " • " + zone + " • " + rr
                        : "Direction • Zone • TF • RR",
                planComplete(),
                v -> showEntryPlan());

        boolean risk = bos || idm || (!bos && !idm);
        addChecklistRow(p, "7", "Risk Management",
                "BOS / IDM optional • risk discipline",
                risk,
                v -> showRisk());

        confirmButton = pinkButton("CEK & BUKA GATE   🔒");
        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(-1, dp(55));
        cp.setMargins(0, dp(10), 0, dp(10));
        p.addView(confirmButton, cp);

        confirmButton.setOnClickListener(v -> showConfirmation());

        scroll.addView(p);
        content.addView(scroll);
    }

    void addChecklistRow(
            LinearLayout parent,
            String number,
            String title,
            String sub,
            boolean done,
            View.OnClickListener click) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(8), dp(8));
        row.setBackground(
                rounded(done
                        ? Color.rgb(30, 21, 27)
                        : CARD, done ? Color.rgb(94, 54, 70) : BORDER, 1, 14));

        TextView check = centerText(done ? "✓" : number, 14,
                done ? Color.rgb(35, 20, 27) : PINK_SOFT);
        check.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        check.setBackground(rounded(
                done ? PINK_SOFT : CARD_2,
                done ? PINK_SOFT : Color.rgb(78, 68, 78),
                1, 8));

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(dp(32), dp(32));
        row.addView(check, cp);

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(10), 0, dp(5), 0);

        TextView t = text(title, 14);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView s = text(sub, 10);
        s.setTextColor(MUTED);

        words.addView(t);
        words.addView(s);

        row.addView(words,
                new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = centerText("›", 23, MUTED);
        row.addView(arrow,
                new LinearLayout.LayoutParams(dp(24), dp(40)));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, dp(58));
        lp.bottomMargin = dp(7);

        parent.addView(row, lp);
        row.setOnClickListener(click);
    }

    // =========================================================
    // CONDITION
    // =========================================================

    void showCondition() {
        clearContent();

        LinearLayout p = pageRoot();
        pageHeader(p, "KONDISI DIRI");

        LinearLayout c = card();
        section(c, "01", "KONDISI DIRI",
                "Gate tidak dibuka kalau kamu sedang tidak siap.");

        CheckBox ok = check("Saya fokus dan kondisi mental stabil.");
        ok.setChecked(conditionOk);
        c.addView(ok);

        Button save = pinkButton("SIMPAN");
        c.addView(save);

        save.setOnClickListener(v -> {
            conditionOk = ok.isChecked();
            showChecklist();
        });

        p.addView(c);
        backButton(p, "‹  KEMBALI");
        setPage(p);
    }

    // =========================================================
    // HTF
    // =========================================================

    boolean h4Ok() { return pref.getBoolean("h4", false); }
    boolean h2Ok() { return pref.getBoolean("h2", false); }
    boolean h1Ok() { return pref.getBoolean("h1", false); }

    void showHTF() {
        clearContent();

        LinearLayout p = pageRoot();
        pageHeader(p, "ANALISIS HTF");

        LinearLayout c = card();
        section(c, "02", "HTF & ZONA",
                "Tandai zona yang sudah jelas.");

        CheckBox a = check("H4 sudah jelas");
        CheckBox b = check("H2 sudah jelas");
        CheckBox d = check("H1 sudah jelas");

        a.setChecked(h4Ok());
        b.setChecked(h2Ok());
        d.setChecked(h1Ok());

        c.addView(a);
        c.addView(b);
        c.addView(d);

        Button save = pinkButton("SIMPAN");
        c.addView(save);

        save.setOnClickListener(v -> {
            pref.edit()
                    .putBoolean("h4", a.isChecked())
                    .putBoolean("h2", b.isChecked())
                    .putBoolean("h1", d.isChecked())
                    .apply();
            showChecklist();
        });

        p.addView(c);
        backButton(p, "‹  KEMBALI");
        setPage(p);
    }

    // =========================================================
    // NEWS
    // =========================================================

    void showNews() {
        clearContent();

        LinearLayout p = pageRoot();
        pageHeader(p, "NEWS CHECK");

        LinearLayout c = card();
        section(c, "03", "NEWS BESAR",
                "Checklist manual untuk memastikan kalender sudah diperiksa.");

        CheckBox ok = check("Saya sudah mengecek News Besar / kalender ekonomi.");
        ok.setChecked(newsChecked);
        c.addView(ok);

        Button save = pinkButton("SIMPAN");
        c.addView(save);

        save.setOnClickListener(v -> {
            newsChecked = ok.isChecked();
            showChecklist();
        });

        p.addView(c);
        backButton(p, "‹  KEMBALI");
        setPage(p);
    }

    // =========================================================
    // ENTRY PLAN
    // =========================================================

    boolean planComplete() {
        return !direction.isEmpty()
                && !zone.isEmpty()
                && !timeframe.isEmpty()
                && !rr.isEmpty();
    }

    void showEntryPlan() {
        clearContent();

        ScrollView scroll = pageScroll();
        LinearLayout p = pageRoot();

        pageHeader(p, "ENTRY PLAN");

        LinearLayout c = card();
        section(c, "06", "ENTRY PLAN",
                "Tap pilihan. Tidak perlu mengetik TP / SL.");

        TextView d = text("DIRECTION", 10);
        d.setTextColor(MUTED);
        c.addView(d);

        LinearLayout directions = new LinearLayout(this);
        directions.setOrientation(LinearLayout.HORIZONTAL);

        Button buy = choice("BUY", direction.equals("BUY"));
        Button sell = choice("SELL", direction.equals("SELL"));

        directions.addView(buy, weightButton());
        directions.addView(sell, weightButton());

        c.addView(directions);

        buy.setOnClickListener(v -> {
            direction = "BUY";
            showEntryPlan();
        });

        sell.setOnClickListener(v -> {
            direction = "SELL";
            showEntryPlan();
        });

        TextView z = text("ZONE", 10);
        z.setTextColor(MUTED);
        z.setPadding(0, dp(12), 0, dp(5));
        c.addView(z);

        LinearLayout zones = new LinearLayout(this);
        zones.setOrientation(LinearLayout.VERTICAL);

        addChoiceRow(zones, "FVG", "POI", "FVG + POI", "SUPPORT", "RESISTANCE");
        c.addView(zones);

        TextView tf = text("TIMEFRAME", 10);
        tf.setTextColor(MUTED);
        tf.setPadding(0, dp(12), 0, dp(5));
        c.addView(tf);

        LinearLayout tfs = new LinearLayout(this);
        tfs.setOrientation(LinearLayout.HORIZONTAL);
        addTf(tfs, "M15");
        addTf(tfs, "M30");
        addTf(tfs, "H1");
        addTf(tfs, "H2");
        addTf(tfs, "H4");
        c.addView(tfs);

        TextView r = text("RISK : REWARD", 10);
        r.setTextColor(MUTED);
        r.setPadding(0, dp(12), 0, dp(5));
        c.addView(r);

        LinearLayout rrs = new LinearLayout(this);
        rrs.setOrientation(LinearLayout.HORIZONTAL);
        addRR(rrs, "1:1");
        addRR(rrs, "1:2");
        addRR(rrs, "1:3");
        c.addView(rrs);

        p.addView(c);

        Button save = pinkButton("SIMPAN ENTRY PLAN");
        p.addView(save,
                new LinearLayout.LayoutParams(-1, dp(54)));

        save.setOnClickListener(v -> showChecklist());

        backButton(p, "‹  KEMBALI");

        scroll.addView(p);
        content.addView(scroll);
    }

    LinearLayout.LayoutParams weightButton() {
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, dp(46), 1);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        return lp;
    }

    Button choice(String text, boolean selected) {
        Button b = button(text);
        if (selected) {
            b.setTextColor(Color.rgb(30, 20, 25));
            b.setBackground(rounded(PINK_SOFT, PINK, 0, 12));
        }
        return b;
    }

    void addChoiceRow(
            LinearLayout parent,
            String... values) {

        for (int start = 0; start < values.length; start += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            int end = Math.min(start + 2, values.length);

            for (int i = start; i < end; i++) {
                String value = values[i];
                Button b = choice(value, zone.equals(value));
                row.addView(b, weightButton());

                b.setOnClickListener(v -> {
                    zone = value;
                    showEntryPlan();
                });
            }

            parent.addView(row);
        }
    }

    void addTf(LinearLayout parent, String value) {
        Button b = choice(value, timeframe.equals(value));
        parent.addView(b, weightButton());
        b.setOnClickListener(v -> {
            timeframe = value;
            showEntryPlan();
        });
    }

    void addRR(LinearLayout parent, String value) {
        Button b = choice(value, rr.equals(value));
        parent.addView(b, weightButton());
        b.setOnClickListener(v -> {
            rr = value;
            showEntryPlan();
        });
    }

    // =========================================================
    // RISK MANAGEMENT
    // =========================================================

    void showRisk() {
        clearContent();

        LinearLayout p = pageRoot();
        pageHeader(p, "RISK MANAGEMENT");

        LinearLayout c = card();
        section(c, "07", "RISK MANAGEMENT",
                "BOS dan IDM bersifat opsional.");

        CheckBox b = check("BOS terkonfirmasi (optional)");
        CheckBox i = check("IDM terkonfirmasi (optional)");

        b.setChecked(bos);
        i.setChecked(idm);

        c.addView(b);
        c.addView(i);

        TextView info = text(
                "Liquidity + CISD tetap wajib.\n" +
                "Risk:Reward dipilih di Entry Plan.",
                11);
        info.setTextColor(MUTED);
        info.setPadding(0, dp(10), 0, dp(10));
        c.addView(info);

        Button save = pinkButton("SIMPAN");
        c.addView(save);

        save.setOnClickListener(v -> {
            bos = b.isChecked();
            idm = i.isChecked();
            showChecklist();
        });

        p.addView(c);
        backButton(p, "‹  KEMBALI");
        setPage(p);
    }

    // =========================================================
    // CONFIRMATION
    // =========================================================

    void showConfirmation() {
        clearContent();
        showBottomNav();
        selectNav(1);

        ScrollView scroll = pageScroll();
        LinearLayout p = pageRoot();

        pageHeader(p, "KONFIRMASI");

        LinearLayout c = card();
        String[][] rows = {
                {"Kondisi Diri", conditionOk ? "OK" : "BELUM"},
                {"Analisis HTF", (h4Ok() && h2Ok() && h1Ok()) ? "H4 / H2 / H1" : "BELUM"},
                {"News Check", newsChecked ? "OK" : "BELUM"},
                {"Liquidity", liquidity ? "OK" : "BELUM"},
                {"CISD", cisd ? "OK" : "BELUM"},
                {"Entry Plan", planComplete() ? direction + " • " + zone + " • " + timeframe : "BELUM"},
                {"Risk Management", rr.isEmpty() ? "BELUM" : "RR " + rr}
        };

        for (String[] row : rows) {
            LinearLayout line = new LinearLayout(this);
            line.setGravity(Gravity.CENTER_VERTICAL);
            line.setPadding(dp(4), dp(7), dp(4), dp(7));

            boolean ok = !"BELUM".equals(row[1]);
            TextView dot = centerText(ok ? "✓" : "!", 13,
                    ok ? Color.rgb(35,20,27) : WHITE);
            dot.setBackground(rounded(
                    ok ? PINK_SOFT : Color.rgb(65, 50, 57),
                    ok ? PINK_SOFT : BORDER, 1, 20));
            line.addView(dot, new LinearLayout.LayoutParams(dp(30), dp(30)));

            TextView name = text(row[0], 12);
            name.setPadding(dp(10), 0, 0, 0);
            line.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

            TextView value = text(row[1], 10);
            value.setTextColor(ok ? PINK_SOFT : MUTED);
            line.addView(value);

            c.addView(line);
        }
        p.addView(c);

        LinearLayout note = card();
        note.setGravity(Gravity.CENTER);
        TextView n = centerText(
                "Semua checklist harus sesuai.\n" +
                "Setelah dibuka, Gate aktif selama 15 menit.",
                11, WHITE);
        note.addView(n);
        p.addView(note);

        Button open = pinkButton("CEK & BUKA GATE   🔒");
        p.addView(open, new LinearLayout.LayoutParams(-1, dp(55)));
        open.setOnClickListener(v -> attemptOpenGate());

        Button back = button("‹  KEMBALI");
        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(-1, dp(46));
        bp.topMargin = dp(9);
        p.addView(back, bp);
        back.setOnClickListener(v -> showChecklist());

        scroll.addView(p);
        content.addView(scroll);
    }

    // =========================================================
    // GATE FLOW
    // =========================================================

    void openGateFlow() {
        if (isSessionActive()) {
            showActiveSession();
            return;
        }

        checkDailyReset();

        if (slCount() >= MAX_SL) {
            showLockedToday("3 SL tercapai. Gate tertutup sampai reset 08:00 WITA.");
            return;
        }

        if (accessCount() >= MAX_ACCESS) {
            showLockedToday("Batas 5 akses hari ini sudah tercapai.");
            return;
        }

        showChecklist();
    }

    void attemptOpenGate() {
        checkDailyReset();

        if (isSessionActive()) {
            showActiveSession();
            return;
        }

        if (accessCount() >= MAX_ACCESS) {
            showLockedToday("Batas 5 akses hari ini sudah tercapai.");
            return;
        }

        if (slCount() >= MAX_SL) {
            showLockedToday("3 SL tercapai. Gate tertutup sampai reset 08:00 WITA.");
            return;
        }

        if (!conditionOk) {
            Toast.makeText(this,
                    "Kondisi diri belum dikonfirmasi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!h4Ok() || !h2Ok() || !h1Ok()) {
            Toast.makeText(this,
                    "Analisis H4 / H2 / H1 belum lengkap.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newsChecked) {
            Toast.makeText(this,
                    "News Check belum selesai.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!liquidity) {
            Toast.makeText(this,
                    "Liquidity wajib dikonfirmasi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cisd) {
            Toast.makeText(this,
                    "CISD wajib dikonfirmasi.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!planComplete()) {
            Toast.makeText(this,
                    "Entry Plan belum lengkap.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int next = accessCount() + 1;

        pref.edit()
                .putInt("access_count", next)
                .putBoolean("unlocked", true)
                .putLong("session_until",
                        System.currentTimeMillis() + SESSION_MS)
                .apply();

        showActiveSession();
    }

    boolean isSessionActive() {
        if (pref == null) return false;

        long until = pref.getLong("session_until", 0);

        if (until > System.currentTimeMillis()) {
            return true;
        }

        if (pref.getBoolean("unlocked", false)) {
            lockSession();
        }

        return false;
    }

    void restoreSession() {
        if (isSessionActive()) {
            showActiveSession();
        } else {
            refreshHome();
        }
    }

    void showActiveSession() {
        clearContent();
        showBottomNav();
        selectNav(0);

        ScrollView scroll = pageScroll();
        LinearLayout p = pageRoot();

        pageHeader(p, "SESI AKTIF");

        TimerRingView ring = new TimerRingView(this);
        p.addView(ring, new LinearLayout.LayoutParams(-1, dp(250)));

        sessionStatus = centerText("TRADING SESSION ACTIVE", 10, PINK_SOFT);
        sessionStatus.setLetterSpacing(.12f);
        p.addView(sessionStatus);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.addView(statCard("AKSES HARI INI",
                accessCount() + " / " + MAX_ACCESS), halfParams(false));
        stats.addView(statCard("SL HARI INI",
                slCount() + " / " + MAX_SL), halfParams(true));
        p.addView(stats);

        LinearLayout active = card();
        TextView at = smallLabel("◷  SESI DIMULAI / AUTO LOCK");
        at.setTextColor(PINK);
        TextView ap = text(
                "Gate aktif berdasarkan waktu nyata.\n" +
                "Menutup aplikasi tidak menghentikan timer.",
                11);
        ap.setTextColor(MUTED);
        ap.setPadding(0, dp(5), 0, 0);
        active.addView(at);
        active.addView(ap);
        p.addView(active);

        mt5Button = pinkButton("BUKA MT5   ›");
        p.addView(mt5Button, new LinearLayout.LayoutParams(-1, dp(54)));
        mt5Button.setOnClickListener(v -> open());

        TextView focus = centerText(
                "FOKUS. IKUTI PLAN. DISIPLIN.", 9, MUTED);
        focus.setLetterSpacing(.12f);
        focus.setPadding(0, dp(12), 0, 0);
        p.addView(focus);

        scroll.addView(p);
        content.addView(scroll);

        startSessionTimer(ring);
    }


    LinearLayout.LayoutParams halfParams(boolean right) {
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, -2, 1);
        lp.setMargins(right ? dp(5) : 0, 0, right ? 0 : dp(5), dp(12));
        return lp;
    }

    LinearLayout statCard(String name, String value) {
        LinearLayout c = card();
        c.setPadding(dp(12), dp(10), dp(12), dp(10));

        TextView a = text(name, 9);
        a.setTextColor(MUTED);
        TextView b = text(value, 20);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(PINK_SOFT);

        c.addView(a);
        c.addView(b);
        return c;
    }

    void startSessionTimer() {
        startSessionTimer(null);
    }

    void startSessionTimer(TimerRingView ring) {
        if (timer != null) timer.cancel();

        long left = pref.getLong("session_until", 0)
                - System.currentTimeMillis();

        if (left <= 0) {
            lockSession();
            return;
        }

        timer = new CountDownTimer(left, 1000) {
            @Override
            public void onTick(long ms) {
                long sec = ms / 1000;
                long min = sec / 60;
                long rem = sec % 60;

                String value = String.format(Locale.US, "%02d:%02d", min, rem);

                if (sessionTimer != null) sessionTimer.setText(value);
                if (ring != null) ring.setRemaining(ms, value);
            }

            @Override
            public void onFinish() {
                if (ring != null) ring.setRemaining(0, "00:00");
                lockSession();
            }
        }.start();
    }


    void lockSession() {
        if (timer != null) timer.cancel();

        pref.edit()
                .putBoolean("unlocked", false)
                .putLong("session_until", 0)
                .apply();

        if (content != null) {
            showHome();
        }
    }

    void showLockedToday(String message) {
        clearContent();
        showBottomNav();
        selectNav(0);

        LinearLayout p = pageRoot();
        pageHeader(p, "GATE TERKUNCI");

        LinearLayout c = card();
        c.setGravity(Gravity.CENTER);

        TextView icon = centerText("🔒", 38, PINK_SOFT);
        c.addView(icon);

        TextView h = centerText("BATAS HARI INI TERCAPAI", 19, PINK_SOFT);
        h.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        h.setPadding(0, dp(8), 0, dp(4));

        TextView m = centerText(message, 12, WHITE);
        m.setPadding(dp(10), 0, dp(10), dp(10));

        c.addView(h);
        c.addView(m);
        p.addView(c);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.addView(statCard("AKSES HARI INI",
                accessCount() + " / " + MAX_ACCESS), halfParams(false));
        stats.addView(statCard("SL HARI INI",
                slCount() + " / " + MAX_SL), halfParams(true));
        p.addView(stats);

        LinearLayout next = card();
        TextView n1 = text("NEXT ACCESS", 10);
        n1.setTextColor(PINK);
        TextView n2 = text("Besok 08:00 WITA", 14);
        n2.setTextColor(WHITE);
        next.addView(n1);
        next.addView(n2);
        p.addView(next);

        Button home = pinkButton("KEMBALI KE HOME");
        p.addView(home,
                new LinearLayout.LayoutParams(-1, dp(52)));
        home.setOnClickListener(v -> showHome());

        setPage(p);
    }

    // =========================================================
    // HISTORY
    // =========================================================

    void showHistory() {
        clearContent();
        showBottomNav();
        selectNav(2);

        ScrollView scroll = pageScroll();
        LinearLayout p = pageRoot();

        pageHeader(p, "RIWAYAT");

        LinearLayout today = card();
        section(today, "TODAY", "AKTIVITAS", "Ringkasan sesi Gate hari ini.");

        TextView a = text("Gate dibuka", 12);
        a.setTextColor(MUTED);
        TextView av = text(accessCount() + " sesi", 20);
        av.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        av.setTextColor(PINK_SOFT);

        TextView s = text("Stop Loss terdeteksi", 12);
        s.setTextColor(MUTED);
        TextView sv = text(slCount() + " / " + MAX_SL, 20);
        sv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        sv.setTextColor(PINK_SOFT);

        today.addView(a);
        today.addView(av);
        today.addView(s);
        today.addView(sv);
        p.addView(today);

        LinearLayout note = card();
        TextView nt = text(
                "Riwayat detail transaksi dan deteksi SL otomatis akan diisi setelah koneksi MT5/VPS selesai.",
                11);
        nt.setTextColor(MUTED);
        note.addView(nt);
        p.addView(note);

        scroll.addView(p);
        content.addView(scroll);
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    void showSettings() {
        clearContent();
        showBottomNav();
        selectNav(3);

        LinearLayout p = pageRoot();
        pageHeader(p, "PENGATURAN");

        LinearLayout c = card();
        section(c, "SYSTEM", "TRADING GATE",
                "Pengaturan akses dan koneksi.");

        Button appLock = button("⚙  AKTIFKAN APP LOCK");
        c.addView(appLock,
                new LinearLayout.LayoutParams(-1, dp(48)));

        appLock.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this,
                        "Tidak bisa membuka Accessibility.",
                        Toast.LENGTH_LONG).show();
            }
        });

        Button mt5 = button("BUKA MT5");
        LinearLayout.LayoutParams mp =
                new LinearLayout.LayoutParams(-1, dp(48));
        mp.topMargin = dp(8);
        c.addView(mt5, mp);
        mt5.setOnClickListener(v -> open());

        TextView info = text(
                "App Lock menggunakan Accessibility. "
                        + "Deteksi Stop Loss otomatis belum terhubung.",
                11);
        info.setTextColor(MUTED);
        info.setPadding(0, dp(10), 0, 0);
        c.addView(info);

        p.addView(c);

        LinearLayout about = card();
        section(about, "ABOUT", "TRADING GATE",
                "Discipline System");
        TextView v = text("V3 UI  •  15 min Gate  •  5 access/day", 11);
        v.setTextColor(MUTED);
        about.addView(v);
        p.addView(about);

        setPage(p);
    }

    // =========================================================
    // PAGE HELPERS
    // =========================================================

    void backButton(LinearLayout p, String label) {
        Button b = button(label);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, dp(46));
        lp.topMargin = dp(10);
        p.addView(b, lp);
        b.setOnClickListener(v -> showChecklist());
    }

    void setPage(LinearLayout p) {
        ScrollView s = pageScroll();
        s.addView(p);
        content.addView(s);
    }

    int checklistCount() {
        int n = 0;
        if (conditionOk) n++;
        if (h4Ok() && h2Ok() && h1Ok()) n++;
        if (newsChecked) n++;
        if (liquidity) n++;
        if (cisd) n++;
        if (planComplete()) n++;
        n++; // Risk management: BOS/IDM are optional.
        return n;
    }

    CheckBox check(String label) {
        CheckBox c = new CheckBox(this);
        c.setText(label);
        c.setTextColor(WHITE);
        c.setTextSize(12);
        c.setButtonTintList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        PINK_SOFT,
                        MUTED
                }
        ));
        c.setPadding(0, dp(5), 0, dp(5));
        return c;
    }

    // =========================================================
    // CUSTOM VISUALS
    // =========================================================

    class GateHeroView extends View {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Path path = new Path();

        GateHeroView(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth();
            float h = getHeight();

            paint.setShader(new LinearGradient(
                    0, 0, 0, h,
                    Color.rgb(8, 9, 12),
                    Color.rgb(20, 8, 18),
                    Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            // soft atmospheric glow
            paint.setColor(Color.rgb(75, 25, 54));
            paint.setAlpha(70);
            paint.setMaskFilter(new android.graphics.BlurMaskFilter(
                    dp(38), android.graphics.BlurMaskFilter.Blur.NORMAL));
            c.drawCircle(w * .50f, h * .53f, dp(75), paint);
            paint.setMaskFilter(null);
            paint.setAlpha(255);

            // mountains
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(17, 18, 22));
            path.reset();
            path.moveTo(0, h * .82f);
            path.lineTo(w * .20f, h * .48f);
            path.lineTo(w * .32f, h * .73f);
            path.lineTo(w * .48f, h * .38f);
            path.lineTo(w * .64f, h * .70f);
            path.lineTo(w * .82f, h * .50f);
            path.lineTo(w, h * .78f);
            path.lineTo(w, h);
            path.lineTo(0, h);
            path.close();
            c.drawPath(path, paint);

            // portal glow
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(7));
            paint.setColor(PINK);
            paint.setShadowLayer(dp(24), 0, 0, PINK);
            RectF oval = new RectF(
                    w * .38f, h * .35f,
                    w * .62f, h * .77f);
            c.drawRoundRect(oval, dp(60), dp(60), paint);
            paint.clearShadowLayer();

            paint.setStrokeWidth(dp(2));
            paint.setColor(PINK_SOFT);
            c.drawRoundRect(
                    new RectF(w * .395f, h * .365f,
                            w * .605f, h * .755f),
                    dp(55), dp(55), paint);

            // tiny particles
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 174, 202));
            for (int i = 0; i < 16; i++) {
                float x = (w * ((i * 37) % 100)) / 100f;
                float y = h * (.12f + ((i * 17) % 60) / 100f);
                c.drawCircle(x, y, dp(1), paint);
            }

            // small mark
            paint.setColor(PINK_SOFT);
            path.reset();
            float cx = w / 2f, cy = h * .56f;
            path.moveTo(cx, cy - dp(13));
            path.cubicTo(cx - dp(13), cy - dp(5),
                    cx - dp(13), cy + dp(5),
                    cx, cy + dp(13));
            path.cubicTo(cx + dp(13), cy + dp(5),
                    cx + dp(13), cy - dp(5),
                    cx, cy - dp(13));
            path.close();
            c.drawPath(path, paint);
        }
    }

    class TimerRingView extends View {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        String value = "15:00";
        float progress = 1f;

        TimerRingView(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void setRemaining(long ms, String text) {
            value = text;
            progress = Math.max(0f, Math.min(1f,
                    ms / (float) SESSION_MS));
            invalidate();
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float r = Math.min(getWidth(), getHeight()) * .37f;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(7));
            paint.setColor(Color.rgb(50, 31, 42));
            c.drawCircle(cx, cy, r, paint);

            paint.setColor(PINK);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setShadowLayer(dp(15), 0, 0, PINK);
            c.drawArc(new RectF(cx-r, cy-r, cx+r, cy+r),
                    -90, 360f * progress, false, paint);
            paint.clearShadowLayer();
            paint.setStrokeCap(Paint.Cap.BUTT);

            paint.setStyle(Paint.Style.FILL);
            TextPaintHelper(c, "SISA WAKTU", cx, cy - dp(30), 10, MUTED);
            TextPaintHelper(c, value, cx, cy + dp(10), 34, WHITE);
            TextPaintHelper(c, "/ 15:00", cx, cy + dp(36), 11, PINK_SOFT);
        }

        void TextPaintHelper(Canvas c, String t, float x, float y,
                             float size, int color) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            paint.setTextSize(dp((int)size));
            paint.setTypeface(Typeface.DEFAULT);
            paint.setTextAlign(Paint.Align.CENTER);
            c.drawText(t, x, y, paint);
        }
    }

    // =========================================================
    // MT5
    // =========================================================

    void open() {
        if (!isSessionActive()) {
            Toast.makeText(this,
                    "Gate sudah terkunci. Buka Gate terlebih dahulu.",
                    Toast.LENGTH_LONG).show();
            showHome();
            return;
        }

        Intent i = getPackageManager()
                .getLaunchIntentForPackage(MT5);

        if (i != null) {
            startActivity(i);
        } else {
            Toast.makeText(this,
                    "MT5 tidak ditemukan di HP.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
