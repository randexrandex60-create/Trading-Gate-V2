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

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainActivity extends Activity {

    static final String MT5 = "net.metaquotes.metatrader5";
    static final String PREF = "gate";
    static final long UNLOCK = 15 * 60 * 1000L;

    SharedPreferences p;

    TextView status, reason, timer, now;
    Button unlock, mt5;

    RadioButton busy, notBusy;
    RadioButton tired, notTired;

    CheckBox h4, h2, h1;
    CheckBox liq, cisd, bos, idm;

    EditText news, entry, tp, sl;

    CountDownTimer cd;

    int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView text(String s, float size) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(Color.WHITE);
        v.setTextSize(size);
        v.setPadding(0, dp(5), 0, dp(5));
        return v;
    }

    TextView section(String s) {
        TextView v = text(s, 18);
        v.setTypeface(null, 1);
        v.setPadding(0, dp(18), 0, dp(8));
        return v;
    }

    CheckBox check(String s) {
        CheckBox x = new CheckBox(this);
        x.setText(s);
        x.setTextColor(Color.WHITE);
        x.setTextSize(16);
        return x;
    }

    EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.GRAY);
        e.setTextColor(Color.WHITE);
        e.setTextSize(16);
        e.setSingleLine(true);
        e.setPadding(dp(10), dp(6), dp(10), dp(6));
        e.setBackgroundColor(Color.rgb(28, 28, 28));
        return e;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        p = getSharedPreferences(PREF, 0);

        build();
        restore();
    }

    void build() {

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.BLACK);

        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(dp(18), dp(18), dp(18), dp(28));

        TextView title = text("🔒  TRADING GATE", 25);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER);
        r.addView(title);

        TextView sub = text(
                "Entry hanya boleh kalau checklist benar-benar lengkap.",
                13
        );

        sub.setTextColor(Color.GRAY);
        sub.setGravity(Gravity.CENTER);
        r.addView(sub);

        status = text("STATUS: TERKUNCI", 18);
        status.setTypeface(null, 1);
        status.setGravity(Gravity.CENTER);
        status.setTextColor(Color.RED);
        r.addView(status);

        timer = text("", 14);
        timer.setGravity(Gravity.CENTER);
        timer.setTextColor(Color.GREEN);
        r.addView(timer);

        reason = text("", 13);
        reason.setGravity(Gravity.CENTER);
        reason.setTextColor(Color.RED);
        r.addView(reason);

        // =========================
        // 1-2 KONDISI DIRI
        // =========================

        r.addView(section("1-2. KONDISI DIRI"));

        r.addView(text("Apakah saya sedang sibuk / ada kerjaan lain?", 15));

        RadioGroup g1 = new RadioGroup(this);

        notBusy = new RadioButton(this);
        notBusy.setText("TIDAK — saya fokus");
        notBusy.setTextColor(Color.WHITE);

        busy = new RadioButton(this);
        busy.setText("YA — saya sedang sibuk");
        busy.setTextColor(Color.WHITE);

        g1.addView(notBusy);
        g1.addView(busy);
        r.addView(g1);

        // Pilihan kondisi sibuk bisa dipilih dan dibatalkan
        final RadioButton[] selectedBusy = {null};

        notBusy.setOnClickListener(v -> {

            if (selectedBusy[0] == notBusy) {
                g1.clearCheck();
                selectedBusy[0] = null;
            } else {
                selectedBusy[0] = notBusy;
                g1.check(notBusy.getId());
            }
        });

        busy.setOnClickListener(v -> {

            if (selectedBusy[0] == busy) {
                g1.clearCheck();
                selectedBusy[0] = null;
            } else {
                selectedBusy[0] = busy;
                g1.check(busy.getId());
            }
        });

        r.addView(text("Apakah saya sedang capek / emosi?", 15));

        RadioGroup g2 = new RadioGroup(this);

        notTired = new RadioButton(this);
        notTired.setText("TIDAK — kondisi stabil");
        notTired.setTextColor(Color.WHITE);

        tired = new RadioButton(this);
        tired.setText("YA — capek / emosi");
        tired.setTextColor(Color.WHITE);

        g2.addView(notTired);
        g2.addView(tired);
        r.addView(g2);

        // Pilihan kondisi capek/emosi bisa dipilih dan dibatalkan
        final RadioButton[] selectedTired = {null};

        notTired.setOnClickListener(v -> {

            if (selectedTired[0] == notTired) {
                g2.clearCheck();
                selectedTired[0] = null;
            } else {
                selectedTired[0] = notTired;
                g2.check(notTired.getId());
            }
        });

        tired.setOnClickListener(v -> {

            if (selectedTired[0] == tired) {
                g2.clearCheck();
                selectedTired[0] = null;
            } else {
                selectedTired[0] = tired;
                g2.check(tired.getId());
            }
        });

        // =========================
        // 3-5 ZONA HTF
        // =========================

        r.addView(section("3-5. ZONA HTF"));

        h4 = check("ZONA H4 sudah jelas");
        h2 = check("ZONA H2 sudah jelas");
        h1 = check("ZONA H1 sudah jelas");

        r.addView(h4);
        r.addView(h2);
        r.addView(h1);

        // =========================
        // 6 NEWS BESAR
        // =========================

        r.addView(section("6. NEWS BESAR"));

        r.addView(text(
                "Isi jam News Besar dalam WITA. Gate hanya boleh dibuka mulai 2 jam sebelum news sampai waktu news.",
                14
        ));

        news = input("Contoh: 20:30 WITA");
        r.addView(news);

        now = text("Waktu WITA sekarang: --:--:--", 13);
        now.setTextColor(Color.GRAY);
        r.addView(now);

        updateNow();

        // =========================
        // 7-10 KONFIRMASI MARKET
        // =========================

        r.addView(section("7-10. KONFIRMASI MARKET"));

        liq = check("LIQUIDITY");
        cisd = check("CISD");
        bos = check("BOS");
        idm = check("IDM");

        r.addView(liq);
        r.addView(cisd);
        r.addView(bos);
        r.addView(idm);

        // =========================
        // 11-13 PLAN
        // =========================

        r.addView(section("11-13. PLAN ENTRY"));

        r.addView(text("Plan Entry", 14));
        entry = input("Contoh: Sell setelah CISD di POI H1");
        r.addView(entry);

        r.addView(text("TP", 14));
        tp = input("Contoh: target harga / pip");
        r.addView(tp);

        r.addView(text("SL", 14));
        sl = input("Contoh: invalidasi harga / pip");
        r.addView(sl);

        // =========================
        // UNLOCK
        // =========================

        unlock = new Button(this);
        unlock.setText("🔒  CEK & UNLOCK MT5");
        unlock.setOnClickListener(v -> checkUnlock());

        LinearLayout.LayoutParams unlockParams =
                new LinearLayout.LayoutParams(-1, dp(55));

        unlockParams.topMargin = dp(18);

        r.addView(unlock, unlockParams);

        // =========================
        // BUKA MT5
        // =========================

        mt5 = new Button(this);
        mt5.setText("BUKA MT5");
        mt5.setEnabled(false);
        mt5.setOnClickListener(v -> open());

        r.addView(mt5, new LinearLayout.LayoutParams(-1, dp(55)));

        // =========================
        // APP LOCK
        // =========================

        Button appLock = new Button(this);
        appLock.setText("⚙  AKTIFKAN APP LOCK");

        appLock.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                );
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Tidak bisa membuka pengaturan Accessibility.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        r.addView(appLock, new LinearLayout.LayoutParams(-1, dp(55)));

        TextView info = text(
                "Aktifkan Trading Gate di Pengaturan > Aksesibilitas agar MT5 yang dibuka langsung ikut ditahan saat gate terkunci.",
                12
        );

        info.setTextColor(Color.GRAY);
        r.addView(info);

        sv.addView(r);
        setContentView(sv);
    }

    // =========================
    // WAKTU WITA
    // =========================

    void updateNow() {

        ZonedDateTime z =
                ZonedDateTime.now(ZoneId.of("Asia/Makassar"));

        now.setText(
                "Waktu WITA sekarang: " +
                        z.format(
                                DateTimeFormatter.ofPattern(
                                        "HH:mm:ss"
                                )
                        )
        );
    }

    // =========================
    // VALIDASI
    // =========================

    String validate() {

        // Harus memilih kondisi sibuk/fokus
        if (!busy.isChecked() && !notBusy.isChecked()) {
            return "LOCK: pilih kondisi sibuk/fokus terlebih dahulu.";
        }

        if (busy.isChecked()) {
            return "LOCK: kamu sedang sibuk.";
        }

        // Harus memilih kondisi capek/stabil
        if (!tired.isChecked() && !notTired.isChecked()) {
            return "LOCK: pilih kondisi capek/stabil terlebih dahulu.";
        }

        if (tired.isChecked()) {
            return "LOCK: kamu sedang capek / emosi.";
        }

        if (!h4.isChecked() ||
                !h2.isChecked() ||
                !h1.isChecked()) {

            return "LOCK: zona H4, H2, H1 harus jelas.";
        }

        if (!liq.isChecked()) {
            return "LOCK: Liquidity belum dikonfirmasi.";
        }

        if (!cisd.isChecked()) {
            return "LOCK: CISD belum dikonfirmasi.";
        }

        if (!bos.isChecked()) {
            return "LOCK: BOS belum dikonfirmasi.";
        }

        if (!idm.isChecked()) {
            return "LOCK: IDM belum dikonfirmasi.";
        }

        if (entry.getText().toString().trim().isEmpty()) {
            return "LOCK: Plan Entry wajib diisi.";
        }

        if (tp.getText().toString().trim().isEmpty()) {
            return "LOCK: TP wajib diisi.";
        }

        if (sl.getText().toString().trim().isEmpty()) {
            return "LOCK: SL wajib diisi.";
        }

        // =========================
        // VALIDASI NEWS WITA
        // =========================

        String s = news.getText().toString().trim().toUpperCase(Locale.ROOT);
        s = s.replace("WITA", "").trim();

        if (!s.matches("\\d{2}:\\d{2}")) {
            return "LOCK: isi jam News Besar dengan format HH:mm atau HH:mm WITA.";
        }

        int h;
        int m;

        try {
            h = Integer.parseInt(s.substring(0, 2));
            m = Integer.parseInt(s.substring(3, 5));
        } catch (Exception e) {
            return "LOCK: format jam News tidak valid.";
        }

        if (h > 23 || m > 59) {
            return "LOCK: jam News tidak valid.";
        }

        ZoneId wita = ZoneId.of("Asia/Makassar");

        ZonedDateTime current =
                ZonedDateTime.now(wita);

        LocalDateTime newsDateTime =
                LocalDateTime.of(
                        current.toLocalDate(),
                        LocalTime.of(h, m)
                );

        ZonedDateTime newsTime =
                newsDateTime.atZone(wita);

        ZonedDateTime unlockTime =
                newsTime.minusHours(2);

        if (current.isBefore(unlockTime)) {

            String jamBuka =
                    unlockTime.format(
                            DateTimeFormatter.ofPattern("HH:mm")
                    );

            return "LOCK: belum masuk window 2 jam sebelum news. Gate mulai " +
                    jamBuka +
                    " WITA.";
        }

        if (current.isAfter(newsTime)) {
            return "LOCK: waktu News Besar sudah lewat.";
        }

        return null;
    }

    // =========================
    // CEK UNLOCK
    // =========================

    void checkUnlock() {

        String e = validate();

        if (e != null) {

            status.setText("STATUS: TERKUNCI");
            status.setTextColor(Color.RED);

            reason.setText(e);

            mt5.setEnabled(false);

            return;
        }

        p.edit()
                .putBoolean("unlocked", true)
                .putLong(
                        "until",
                        System.currentTimeMillis() + UNLOCK
                )
                .apply();

        status.setText("STATUS: TERBUKA");
        status.setTextColor(Color.GREEN);

        reason.setText(
                "Checklist lolos. Gate aktif 15 menit."
        );

        mt5.setEnabled(true);

        unlock.setText("✓ GATE TERBUKA");

        startTimer();
    }

    // =========================
    // TIMER
    // =========================

    void startTimer() {

        if (cd != null) {
            cd.cancel();
        }

        long left =
                p.getLong("until", 0)
                        - System.currentTimeMillis();

        if (left <= 0) {
            lock();
            return;
        }

        cd = new CountDownTimer(left, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                long totalSeconds =
                        millisUntilFinished / 1000;

                long minutes =
                        totalSeconds / 60;

                long seconds =
                        totalSeconds % 60;

                timer.setText(
                        String.format(
                                Locale.US,
                                "MT5 terbuka: %02d:%02d",
                                minutes,
                                seconds
                        )
                );
            }

            @Override
            public void onFinish() {
                lock();
            }

        }.start();
    }

    // =========================
    // LOCK KEMBALI
    // =========================

    void lock() {

        if (cd != null) {
            cd.cancel();
        }

        p.edit()
                .putBoolean("unlocked", false)
                .putLong("until", 0)
                .apply();

        status.setText("STATUS: TERKUNCI");
        status.setTextColor(Color.RED);

        timer.setText("");

        reason.setText(
                "Waktu habis. Checklist harus diulang."
        );

        mt5.setEnabled(false);

        unlock.setText("🔒  CEK & UNLOCK MT5");

        h4.setChecked(false);
        h2.setChecked(false);
        h1.setChecked(false);

        liq.setChecked(false);
        cisd.setChecked(false);
        bos.setChecked(false);
        idm.setChecked(false);

        entry.setText("");
        tp.setText("");
        sl.setText("");
    }

    // =========================
    // RESTORE STATUS
    // =========================

    void restore() {

        boolean unlocked =
                p.getBoolean("unlocked", false);

        long until =
                p.getLong("until", 0);

        if (unlocked &&
                until > System.currentTimeMillis()) {

            status.setText("STATUS: TERBUKA");
            status.setTextColor(Color.GREEN);

            reason.setText(
                    "Gate masih aktif."
            );

            mt5.setEnabled(true);

            unlock.setText("✓ GATE TERBUKA");

            startTimer();

        } else {

            lock();
        }
    }

    // =========================
    // BUKA MT5
    // =========================

    void open() {

        Intent i =
                getPackageManager()
                        .getLaunchIntentForPackage(MT5);

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
}
