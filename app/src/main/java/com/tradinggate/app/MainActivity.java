package com.tradinggate.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class MainActivity extends Activity {

    static final String MT5 = "net.metaquotes.metatrader5";
    static final String PREF = "trading_gate_v3";

    static final long GATE_DURATION = 15 * 60 * 1000L;

    static final int MAX_ACCESS = 5;
    static final int MAX_SL = 3;

    final ZoneId WITA = ZoneId.of("Asia/Makassar");

    SharedPreferences p;

    TextView status;
    TextView timer;
    TextView accessText;
    TextView slText;
    TextView resetText;
    TextView reason;
    TextView now;

    Button unlock;
    Button mt5;

    RadioButton busy;
    RadioButton tired;

    CheckBox h4;
    CheckBox h2;
    CheckBox h1;

    CheckBox liquidity;
    CheckBox cisd;
    CheckBox bos;
    CheckBox idm;

    EditText news;

    String direction = "";
    String zone = "";
    String timeframe = "";
    String rr = "";

    CountDownTimer cd;

    int dp(int n) {
        return (int) (
                n * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    TextView text(String s, float size) {

        TextView v = new TextView(this);

        v.setText(s);
        v.setTextColor(Color.WHITE);
        v.setTextSize(size);

        v.setPadding(
                0,
                dp(5),
                0,
                dp(5)
        );

        return v;
    }

    TextView section(String s) {

        TextView v = text(s, 18);

        v.setTypeface(null, 1);

        v.setPadding(
                0,
                dp(18),
                0,
                dp(8)
        );

        return v;
    }

    CheckBox check(String s) {

        CheckBox x = new CheckBox(this);

        x.setText(s);
        x.setTextColor(Color.WHITE);
        x.setTextSize(16);

        return x;
    }

    Button choiceButton(String s) {

        Button b = new Button(this);

        b.setText(s);
        b.setTextSize(13);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(35, 35, 35));

        return b;
    }

    EditText input(String hint) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(Color.GRAY);
        e.setTextColor(Color.WHITE);
        e.setTextSize(16);

        e.setSingleLine(true);

        e.setPadding(
                dp(10),
                dp(6),
                dp(10),
                dp(6)
        );

        e.setBackgroundColor(
                Color.rgb(28, 28, 28)
        );

        return e;
    }

    @Override
    public void onCreate(Bundle b) {

        super.onCreate(b);

        p = getSharedPreferences(PREF, 0);

        checkDailyReset();

        build();

        restore();
    }

    // =========================================================
    // BUILD UI
    // =========================================================

    void build() {

        ScrollView sv = new ScrollView(this);

        sv.setBackgroundColor(Color.BLACK);

        LinearLayout r = new LinearLayout(this);

        r.setOrientation(
                LinearLayout.VERTICAL
        );

        r.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(30)
        );

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        TextView title =
                text("🌸  TRADING GATE", 26);

        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER);

        r.addView(title);

        TextView sub =
                text(
                        "DISCIPLINE SYSTEM",
                        12
                );

        sub.setGravity(Gravity.CENTER);
        sub.setTextColor(Color.GRAY);

        r.addView(sub);

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        status =
                text(
                        "STATUS: READY",
                        20
                );

        status.setTypeface(null, 1);
        status.setGravity(Gravity.CENTER);
        status.setTextColor(Color.GREEN);

        r.addView(status);

        // -----------------------------------------------------
        // ACCESS
        // -----------------------------------------------------

        accessText =
                text(
                        "ACCESS 0/5",
                        16
                );

        accessText.setGravity(Gravity.CENTER);

        r.addView(accessText);

        // -----------------------------------------------------
        // SL
        // -----------------------------------------------------

        slText =
                text(
                        "SL 0/3",
                        16
                );

        slText.setGravity(Gravity.CENTER);

        r.addView(slText);

        // -----------------------------------------------------
        // RESET
        // -----------------------------------------------------

        resetText =
                text(
                        "",
                        13
                );

        resetText.setGravity(Gravity.CENTER);
        resetText.setTextColor(Color.GRAY);

        r.addView(resetText);

        // -----------------------------------------------------
        // TIMER
        // -----------------------------------------------------

        timer =
                text(
                        "",
                        18
                );

        timer.setGravity(Gravity.CENTER);
        timer.setTextColor(Color.GREEN);

        r.addView(timer);

        // -----------------------------------------------------
        // REASON
        // -----------------------------------------------------

        reason =
                text(
                        "",
                        13
                );

        reason.setGravity(Gravity.CENTER);
        reason.setTextColor(Color.RED);

        r.addView(reason);

        // -----------------------------------------------------
        // KONDISI DIRI
        // -----------------------------------------------------

        r.addView(
                section(
                        "1-2. KONDISI DIRI"
                )
        );

        r.addView(
                text(
                        "Apakah saya sedang sibuk / ada kerjaan lain?",
                        15
                )
        );

        RadioGroup g1 =
                new RadioGroup(this);

        RadioButton notBusy =
                new RadioButton(this);

        notBusy.setText(
                "TIDAK — saya fokus"
        );

        notBusy.setTextColor(Color.WHITE);
        notBusy.setChecked(true);

        busy =
                new RadioButton(this);

        busy.setText(
                "YA — saya sedang sibuk"
        );

        busy.setTextColor(Color.WHITE);

        g1.addView(notBusy);
        g1.addView(busy);

        r.addView(g1);

        r.addView(
                text(
                        "Apakah saya sedang capek / emosi?",
                        15
                )
        );

        RadioGroup g2 =
                new RadioGroup(this);

        RadioButton notTired =
                new RadioButton(this);

        notTired.setText(
                "TIDAK — kondisi stabil"
        );

        notTired.setTextColor(Color.WHITE);
        notTired.setChecked(true);

        tired =
                new RadioButton(this);

        tired.setText(
                "YA — capek / emosi"
        );

        tired.setTextColor(Color.WHITE);

        g2.addView(notTired);
        g2.addView(tired);

        r.addView(g2);

        // -----------------------------------------------------
        // ZONA HTF
        // -----------------------------------------------------

        r.addView(
                section(
                        "3-5. ZONA HTF"
                )
        );

        h4 =
                check(
                        "ZONA H4 sudah jelas"
                );

        h2 =
                check(
                        "ZONA H2 sudah jelas"
                );

        h1 =
                check(
                        "ZONA H1 sudah jelas"
                );

        r.addView(h4);
        r.addView(h2);
        r.addView(h1);

        // -----------------------------------------------------
        // NEWS
        // -----------------------------------------------------

        r.addView(
                section(
                        "6. NEWS BESAR"
                )
        );

        r.addView(
                text(
                        "Isi jam News Besar dalam WITA.",
                        14
                )
        );

        news =
                input(
                        "Contoh: 20:30"
                );

        r.addView(news);

        now =
                text(
                        "",
                        13
                );

        now.setTextColor(Color.GRAY);

        r.addView(now);

        updateNow();

        // -----------------------------------------------------
        // CONFIRMATION
        // -----------------------------------------------------

        r.addView(
                section(
                        "7-10. KONFIRMASI MARKET"
                )
        );

        liquidity =
                check(
                        "LIQUIDITY  • WAJIB"
                );

        cisd =
                check(
                        "CISD  • WAJIB"
                );

        bos =
                check(
                        "BOS  • OPSIONAL"
                );

        idm =
                check(
                        "IDM  • OPSIONAL"
                );

        r.addView(liquidity);
        r.addView(cisd);
        r.addView(bos);
        r.addView(idm);

        // -----------------------------------------------------
        // ENTRY PLAN
        // -----------------------------------------------------

        r.addView(
                section(
                        "11. ENTRY PLAN"
                )
        );

        r.addView(
                text(
                        "Direction",
                        14
                )
        );

        LinearLayout dir =
                new LinearLayout(this);

        dir.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button buy =
                choiceButton("BUY");

        Button sell =
                choiceButton("SELL");

        dir.addView(
                buy,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        dir.addView(
                sell,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        buy.setOnClickListener(
                v -> {
                    direction = "BUY";
                    buy.setBackgroundColor(
                            Color.rgb(90, 45, 75)
                    );
                    sell.setBackgroundColor(
                            Color.rgb(35, 35, 35)
                    );
                }
        );

        sell.setOnClickListener(
                v -> {
                    direction = "SELL";
                    sell.setBackgroundColor(
                            Color.rgb(90, 45, 75)
                    );
                    buy.setBackgroundColor(
                            Color.rgb(35, 35, 35)
                    );
                }
        );

        r.addView(dir);

        // -----------------------------------------------------
        // ZONE
        // -----------------------------------------------------

        r.addView(
                text(
                        "Zone",
                        14
                )
        );

        LinearLayout zoneRow =
                new LinearLayout(this);

        zoneRow.setOrientation(
                LinearLayout.VERTICAL
        );

        String[] zones = {
                "FVG",
                "POI",
                "FVG + POI",
                "SUPPORT",
                "RESISTANCE"
        };

        for (String z : zones) {

            Button b =
                    choiceButton(z);

            b.setOnClickListener(
                    v -> {

                        zone = z;

                        for (
                                int i = 0;
                                i < zoneRow.getChildCount();
                                i++
                        ) {

                            View child =
                                    zoneRow.getChildAt(i);

                            child.setBackgroundColor(
                                    Color.rgb(
                                            35,
                                            35,
                                            35
                                    )
                            );
                        }

                        b.setBackgroundColor(
                                Color.rgb(
                                        90,
                                        45,
                                        75
                                )
                        );
                    }
            );

            zoneRow.addView(
                    b,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(48)
                    )
            );
        }

        r.addView(zoneRow);

        // -----------------------------------------------------
        // TIMEFRAME
        // -----------------------------------------------------

        r.addView(
                text(
                        "Timeframe",
                        14
                )
        );

        LinearLayout tf =
                new LinearLayout(this);

        tf.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] timeframes = {
                "M15",
                "M30",
                "H1",
                "H2",
                "H4"
        };

        for (String t : timeframes) {

            Button b =
                    choiceButton(t);

            b.setOnClickListener(
                    v -> {

                        timeframe = t;

                        for (
                                int i = 0;
                                i < tf.getChildCount();
                                i++
                        ) {

                            tf.getChildAt(i)
                                    .setBackgroundColor(
                                            Color.rgb(
                                                    35,
                                                    35,
                                                    35
                                            )
                                    );
                        }

                        b.setBackgroundColor(
                                Color.rgb(
                                        90,
                                        45,
                                        75
                                )
                        );
                    }
            );

            tf.addView(
                    b,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(50),
                            1
                    )
            );
        }

        r.addView(tf);

        // -----------------------------------------------------
        // RR
        // -----------------------------------------------------

        r.addView(
                text(
                        "Risk : Reward",
                        14
                )
        );

        LinearLayout rrRow =
                new LinearLayout(this);

        rrRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] rrs = {
                "1 : 1",
                "1 : 2",
                "1 : 3"
        };

        for (String value : rrs) {

            Button b =
                    choiceButton(value);

            b.setOnClickListener(
                    v -> {

                        rr = value;

                        for (
                                int i = 0;
                                i < rrRow.getChildCount();
                                i++
                        ) {

                            rrRow.getChildAt(i)
                                    .setBackgroundColor(
                                            Color.rgb(
                                                    35,
                                                    35,
                                                    35
                                            )
                                    );
                        }

                        b.setBackgroundColor(
                                Color.rgb(
                                        90,
                                        45,
                                        75
                                )
                        );
                    }
            );

            rrRow.addView(
                    b,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(55),
                            1
                    )
            );
        }

        r.addView(rrRow);

        // -----------------------------------------------------
        // UNLOCK
        // -----------------------------------------------------

        unlock =
                new Button(this);

        unlock.setText(
                "🔒  CEK & BUKA GATE"
        );

        unlock.setOnClickListener(
                v -> checkUnlock()
        );

        LinearLayout.LayoutParams up =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        up.topMargin =
                dp(20);

        r.addView(
                unlock,
                up
        );

        // -----------------------------------------------------
        // MT5
        // -----------------------------------------------------

        mt5 =
                new Button(this);

        mt5.setText(
                "BUKA MT5"
        );

        mt5.setEnabled(false);

        mt5.setOnClickListener(
                v -> open()
        );

        r.addView(
                mt5,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        // -----------------------------------------------------
        // APP LOCK
        // -----------------------------------------------------

        Button appLock =
                new Button(this);

        appLock.setText(
                "⚙  AKTIFKAN APP LOCK"
        );

        appLock.setOnClickListener(
                v -> {

                    try {

                        startActivity(
                                new Intent(
                                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                                )
                        );

                    } catch (Exception e) {

                        Toast.makeText(
                                this,
                                "Tidak bisa membuka Accessibility.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        r.addView(
                appLock,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        TextView info =
                text(
                        "Trading Gate membatasi akses MT5 berdasarkan sesi Gate. "
                                + "Batas akses reset setiap hari pukul 08:00 WITA.",
                        12
                );

        info.setTextColor(Color.GRAY);

        r.addView(info);

        sv.addView(r);

        setContentView(sv);
    }

    // =========================================================
    // DAILY RESET 08:00
    // =========================================================

    void checkDailyReset() {

        String savedDate =
                p.getString(
                        "reset_date",
                        ""
                );

        ZonedDateTime now =
                ZonedDateTime.now(WITA);

        LocalDate today =
                now.toLocalDate();

        LocalTime resetTime =
                LocalTime.of(8, 0);

        LocalDate resetDate;

        if (now.toLocalTime().isBefore(resetTime)) {

            resetDate =
                    today.minusDays(1);

        } else {

            resetDate =
                    today;
        }

        String currentResetDate =
                resetDate.toString();

        if (!currentResetDate.equals(savedDate)) {

            p.edit()
                    .putInt("access_count", 0)
                    .putInt("sl_count", 0)
                    .putBoolean("unlocked", false)
                    .putLong("until", 0)
                    .putString(
                            "reset_date",
                            currentResetDate
                    )
                    .apply();
        }
    }

    // =========================================================
    // RESET TEXT
    // =========================================================

    void updateResetText() {

        ZonedDateTime now =
                ZonedDateTime.now(WITA);

        ZonedDateTime nextReset;

        if (now.toLocalTime().isBefore(
                LocalTime.of(8, 0)
        )) {

            nextReset =
                    ZonedDateTime.of(
                            now.toLocalDate(),
                            LocalTime.of(8, 0),
                            WITA
                    );

        } else {

            nextReset =
                    ZonedDateTime.of(
                            now.toLocalDate().plusDays(1),
                            LocalTime.of(8, 0),
                            WITA
                    );
        }

        String label;

        if (
                nextReset.toLocalDate()
                        .equals(now.toLocalDate())
        ) {

            label = "Reset hari ini ";

        } else {

            label = "Reset besok ";
        }

        resetText.setText(
                label +
                        nextReset.format(
                                DateTimeFormatter.ofPattern(
                                        "HH:mm"
                                )
                        ) +
                        " WITA"
        );
    }

    // =========================================================
    // UPDATE DASHBOARD
    // =========================================================

    void updateDashboard() {

        int access =
                p.getInt(
                        "access_count",
                        0
                );

        int sl =
                p.getInt(
                        "sl_count",
                        0
                );

        accessText.setText(
                "ACCESS  " +
                        access +
                        "/" +
                        MAX_ACCESS
        );

        slText.setText(
                "SL  " +
                        sl +
                        "/" +
                        MAX_SL
        );

        updateResetText();
    }

    // =========================================================
    // WAKTU WITA
    // =========================================================

    void updateNow() {

        ZonedDateTime z =
                ZonedDateTime.now(WITA);

        if (now != null) {

            now.setText(
                    "Waktu WITA sekarang: " +
                            z.format(
                                    DateTimeFormatter.ofPattern(
                                            "HH:mm:ss"
                                    )
                            )
            );
        }

        updateResetText();
    }

    // =========================================================
    // VALIDASI
    // =========================================================

    String validate() {

        checkDailyReset();

        int access =
                p.getInt(
                        "access_count",
                        0
                );

        int slCount =
                p.getInt(
                        "sl_count",
                        0
                );

        if (access >= MAX_ACCESS) {

            return "LOCK: batas 5 akses hari ini sudah habis.";
        }

        if (slCount >= MAX_SL) {

            return "LOCK: 3 SL sudah tercapai. Gate terkunci sampai 08:00 WITA.";
        }

        if (busy.isChecked()) {

            return "LOCK: kamu sedang sibuk.";
        }

        if (tired.isChecked()) {

            return "LOCK: kamu sedang capek / emosi.";
        }

        if (
                !h4.isChecked() ||
                !h2.isChecked() ||
                !h1.isChecked()
        ) {

            return "LOCK: zona H4, H2, H1 harus jelas.";
        }

        if (!liquidity.isChecked()) {

            return "LOCK: Liquidity wajib dikonfirmasi.";
        }

        if (!cisd.isChecked()) {

            return "LOCK: CISD wajib dikonfirmasi.";
        }

        if (direction.isEmpty()) {

            return "LOCK: pilih BUY atau SELL.";
        }

        if (zone.isEmpty()) {

            return "LOCK: pilih zona entry.";
        }

        if (timeframe.isEmpty()) {

            return "LOCK: pilih timeframe.";
        }

        if (rr.isEmpty()) {

            return "LOCK: pilih Risk : Reward.";
        }

        String s =
                news.getText()
                        .toString()
                        .trim();

        if (!s.matches(
                "\\d{2}:\\d{2}"
        )) {

            return "LOCK: isi jam News dengan format HH:mm.";
        }

        int h;
        int m;

        try {

            h =
                    Integer.parseInt(
                            s.substring(0, 2)
                    );

            m =
                    Integer.parseInt(
                            s.substring(3, 5)
                    );

        } catch (Exception e) {

            return "LOCK: format jam News tidak valid.";
        }

        if (
                h > 23 ||
                m > 59
        ) {

            return "LOCK: jam News tidak valid.";
        }

        ZonedDateTime current =
                ZonedDateTime.now(WITA);

        LocalDateTime newsDateTime =
                LocalDateTime.of(
                        current.toLocalDate(),
                        LocalTime.of(h, m)
                );

        ZonedDateTime newsTime =
                newsDateTime.atZone(WITA);

        ZonedDateTime gateStart =
                newsTime.minusHours(2);

        if (current.isBefore(gateStart)) {

            return "LOCK: belum masuk window 2 jam sebelum News.";
        }

        if (current.isAfter(newsTime)) {

            return "LOCK: waktu News Besar sudah lewat.";
        }

        return null;
    }

    // =========================================================
    // CEK & BUKA GATE
    // =========================================================

    void checkUnlock() {

        String error =
                validate();

        if (error != null) {

            status.setText(
                    "STATUS: LOCKED"
            );

            status.setTextColor(
                    Color.RED
            );

            reason.setText(error);

            mt5.setEnabled(false);

            updateDashboard();

            return;
        }

        int access =
                p.getInt(
                        "access_count",
                        0
                );

        access++;

        long until =
                System.currentTimeMillis()
                        + GATE_DURATION;

        p.edit()
                .putInt(
                        "access_count",
                        access
                )
                .putBoolean(
                        "unlocked",
                        true
                )
                .putLong(
                        "until",
                        until
                )
                .apply();

        status.setText(
                "STATUS: ACTIVE"
        );

        status.setTextColor(
                Color.GREEN
        );

        reason.setText(
                "Gate aktif. Gunakan waktu ini sesuai plan."
        );

        unlock.setText(
                "✓ GATE AKTIF"
        );

        mt5.setEnabled(true);

        updateDashboard();

        startTimer();
    }

    // =========================================================
    // TIMER
    // =========================================================

    void startTimer() {

        if (cd != null) {

            cd.cancel();
        }

        long until =
                p.getLong(
                        "until",
                        0
                );

        long left =
                until -
                        System.currentTimeMillis();

        if (left <= 0) {

            lock();

            return;
        }

        cd =
                new CountDownTimer(
                        left,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millis
                    ) {

                        long total =
                                millis / 1000;

                        long minutes =
                                total / 60;

                        long seconds =
                                total % 60;

                        timer.setText(
                                String.format(
                                        Locale.US,
                                        "GATE ACTIVE  %02d:%02d",
                                        minutes,
                                        seconds
                                )
                        );

                        updateNow();
                    }

                    @Override
                    public void onFinish() {

                        lock();
                    }

                }.start();
    }

    // =========================================================
    // LOCK
    // =========================================================

    void lock() {

        if (cd != null) {

            cd.cancel();
        }

        p.edit()
                .putBoolean(
                        "unlocked",
                        false
                )
                .putLong(
                        "until",
                        0
                )
                .apply();

        status.setText(
                "STATUS: LOCKED"
        );

        status.setTextColor(
                Color.RED
        );

        timer.setText("");

        reason.setText(
                "Sesi 15 menit selesai. Gate terkunci."
        );

        mt5.setEnabled(false);

        unlock.setText(
                "🔒  CEK & BUKA GATE"
        );

        updateDashboard();
    }

    // =========================================================
    // RESTORE
    // =========================================================

    void restore() {

        checkDailyReset();

        updateDashboard();

        boolean unlocked =
                p.getBoolean(
                        "unlocked",
                        false
                );

        long until =
                p.getLong(
                        "until",
                        0
                );

        int access =
                p.getInt(
                        "access_count",
                        0
                );

        int slCount =
                p.getInt(
                        "sl_count",
                        0
                );

        if (slCount >= MAX_SL) {

            status.setText(
                    "STATUS: LOCKED"
            );

            status.setTextColor(
                    Color.RED
            );

            reason.setText(
                    "3 SL tercapai. Reset 08:00 WITA."
            );

            mt5.setEnabled(false);

            return;
        }

        if (access >= MAX_ACCESS) {

            status.setText(
                    "STATUS: LOCKED"
            );

            status.setTextColor(
                    Color.RED
            );

            reason.setText(
                    "5 akses hari ini sudah habis."
            );

            mt5.setEnabled(false);

            return;
        }

        if (
                unlocked &&
                until > System.currentTimeMillis()
        ) {

            status.setText(
                    "STATUS: ACTIVE"
            );

            status.setTextColor(
                    Color.GREEN
            );

            reason.setText(
                    "Gate masih aktif."
            );

            mt5.setEnabled(true);

            unlock.setText(
                    "✓ GATE AKTIF"
            );

            startTimer();

        } else {

            p.edit()
                    .putBoolean(
                            "unlocked",
                            false
                    )
                    .putLong(
                            "until",
                            0
                    )
                    .apply();

            status.setText(
                    "STATUS: READY"
            );

            status.setTextColor(
                    Color.GREEN
            );

            reason.setText(
                    "Siap membuat plan."
            );

            mt5.setEnabled(false);

            unlock.setText(
                    "🔒  CEK & BUKA GATE"
            );
        }
    }

    // =========================================================
    // MT5
    // =========================================================

    void open() {

        if (
                !p.getBoolean(
                        "unlocked",
                        false
                )
        ) {

            Toast.makeText(
                    this,
                    "Gate sedang terkunci.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent i =
                getPackageManager()
                        .getLaunchIntentForPackage(
                                MT5
                        );

        if (i != null) {

            startActivity(i);

        } else {

            Toast.makeText(
                    this,
                    "MT5 tidak ditemukan di HP.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // CEK SAAT KEMBALI KE APP
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (p != null) {

            checkDailyReset();

            updateDashboard();

            boolean unlocked =
                    p.getBoolean(
                            "unlocked",
                            false
                    );

            long until =
                    p.getLong(
                            "until",
                            0
                    );

            if (
                    unlocked &&
                    until > System.currentTimeMillis()
            ) {

                if (cd == null) {

                    startTimer();
                }

            } else if (unlocked) {

                lock();
            }
        }
    }
}
