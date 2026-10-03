package com.tradinggate.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.Gravity;
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

    static final int BG = Color.rgb(10,10,12);
    static final int CARD = Color.rgb(20,19,23);
    static final int CARD2 = Color.rgb(27,25,30);
    static final int PINK = Color.rgb(224,151,181);
    static final int PINK_SOFT = Color.rgb(244,193,211);
    static final int WHITE = Color.rgb(248,245,247);
    static final int MUTED = Color.rgb(158,151,158);
    static final int GREEN = Color.rgb(145,218,174);
    static final int RED = Color.rgb(235,116,139);

    SharedPreferences p;
    TextView status, reason, timer, now;
    Button unlock, mt5;
    RadioButton busy, notBusy, tired, notTired;
    CheckBox h4,h2,h1,liq,cisd,bos,idm;
    EditText news,entry,tp,sl;
    CountDownTimer cd;

    int dp(int n) {
        return (int)(n * getResources().getDisplayMetrics().density + .5f);
    }

    GradientDrawable shape(int color, int stroke, int width, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        if (width > 0) g.setStroke(dp(width), stroke);
        return g;
    }

    TextView tv(String s, float size) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(WHITE);
        v.setTextSize(size);
        return v;
    }

    LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15),dp(14),dp(15),dp(14));
        c.setBackground(shape(CARD,Color.rgb(55,49,57),1,18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin = dp(12);
        c.setLayoutParams(lp);
        return c;
    }

    void heading(LinearLayout c, String title, String sub) {
        TextView a = tv(title,16);
        a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        a.setTextColor(PINK_SOFT);
        c.addView(a);
        TextView b = tv(sub,11);
        b.setTextColor(MUTED);
        b.setPadding(0,dp(3),0,dp(10));
        c.addView(b);
    }

    CheckBox check(String s) {
        CheckBox x = new CheckBox(this);
        x.setText(s);
        x.setTextColor(WHITE);
        x.setTextSize(14);
        x.setButtonTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},
                new int[]{PINK,Color.rgb(100,95,102)}));
        x.setPadding(0,dp(2),0,dp(2));
        return x;
    }

    void radioStyle(RadioButton r) {
        r.setTextColor(WHITE);
        r.setTextSize(14);
        r.setButtonTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},
                new int[]{PINK,Color.rgb(100,95,102)}));
    }

    EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(Color.rgb(105,99,106));
        e.setTextColor(WHITE);
        e.setTextSize(14);
        e.setSingleLine(true);
        e.setPadding(dp(12),0,dp(12),0);
        e.setBackground(shape(CARD2,Color.rgb(60,54,62),1,12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,dp(47));
        lp.bottomMargin = dp(8);
        e.setLayoutParams(lp);
        return e;
    }

    Button action(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackground(shape(CARD2,Color.rgb(65,57,65),1,14));
        return b;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        p = getSharedPreferences(PREF,0);
        build();
        restore();
    }

    void build() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(18),dp(16),dp(28));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0,dp(6),0,dp(18));

        TextView brand = tv("TRADING GATE",27);
        brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        brand.setGravity(Gravity.CENTER);

        TextView small = tv("DISCIPLINE SYSTEM",11);
        small.setTextColor(PINK);
        small.setGravity(Gravity.CENTER);
        small.setLetterSpacing(.18f);

        TextView motto = tv("NO SETUP.  NO ENTRY.",12);
        motto.setTextColor(MUTED);
        motto.setGravity(Gravity.CENTER);
        motto.setPadding(0,dp(7),0,0);

        header.addView(brand);
        header.addView(small);
        header.addView(motto);
        root.addView(header);

        LinearLayout statusCard = card();
        TextView cap = tv("GATE STATUS",10);
        cap.setTextColor(MUTED);
        status = tv("TERKUNCI",24);
        status.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        status.setTextColor(RED);
        timer = tv("",13);
        timer.setTextColor(GREEN);
        reason = tv("Lengkapi checklist sebelum membuka MT5.",12);
        reason.setTextColor(MUTED);
        reason.setPadding(0,dp(5),0,0);
        statusCard.addView(cap);
        statusCard.addView(status);
        statusCard.addView(timer);
        statusCard.addView(reason);
        root.addView(statusCard);

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout access = card();
        access.setPadding(dp(12),dp(10),dp(12),dp(10));
        TextView a1 = tv("ACCESS HARI INI",10); a1.setTextColor(MUTED);
        TextView a2 = tv("0 / 5",20); a2.setTextColor(PINK_SOFT);
        a2.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        access.addView(a1); access.addView(a2);

        LinearLayout losses = card();
        losses.setPadding(dp(12),dp(10),dp(12),dp(10));
        TextView l1 = tv("STOP LOSS HARI INI",10); l1.setTextColor(MUTED);
        TextView l2 = tv("0 / 3",20); l2.setTextColor(PINK_SOFT);
        l2.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        losses.addView(l1); losses.addView(l2);

        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0,-2,1);
        half.setMargins(0,0,dp(5),dp(12));
        stats.addView(access,half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0,-2,1);
        half2.setMargins(dp(5),0,0,dp(12));
        stats.addView(losses,half2);
        root.addView(stats);

        TextView reset = tv("Reset 08:00 WITA  •  setiap sesi aktif 15 menit",11);
        reset.setTextColor(MUTED);
        reset.setGravity(Gravity.CENTER);
        reset.setPadding(0,0,0,dp(15));
        root.addView(reset);

        LinearLayout self = card();
        heading(self,"01  KONDISI DIRI","Trading dimulai dari kondisi diri.");

        self.addView(tv("Saya sedang fokus dan tidak ada pekerjaan lain.",13));
        RadioGroup g1 = new RadioGroup(this);
        notBusy = new RadioButton(this); notBusy.setText("Ya — saya fokus");
        busy = new RadioButton(this); busy.setText("Tidak — saya sedang sibuk");
        radioStyle(notBusy); radioStyle(busy); notBusy.setChecked(true);
        g1.addView(notBusy); g1.addView(busy); self.addView(g1);

        self.addView(tv("Kondisi mental saya stabil.",13));
        RadioGroup g2 = new RadioGroup(this);
        notTired = new RadioButton(this); notTired.setText("Ya — kondisi stabil");
        tired = new RadioButton(this); tired.setText("Tidak — capek / emosi");
        radioStyle(notTired); radioStyle(tired); notTired.setChecked(true);
        g2.addView(notTired); g2.addView(tired); self.addView(g2);
        root.addView(self);

        LinearLayout htf = card();
        heading(htf,"02  HTF & ZONA","Pastikan konteks besar sudah jelas.");
        h4=check("ZONA H4 sudah jelas");
        h2=check("ZONA H2 sudah jelas");
        h1=check("ZONA H1 sudah jelas");
        htf.addView(h4); htf.addView(h2); htf.addView(h1);
        root.addView(htf);

        LinearLayout newsCard = card();
        heading(newsCard,"03  NEWS BESAR","Window news mengikuti aturan Gate.");
        TextView nh = tv("Isi jam News Besar dalam WITA. Gate mulai 2 jam sebelum news sampai waktu news.",11);
        nh.setTextColor(MUTED); nh.setPadding(0,0,0,dp(8));
        newsCard.addView(nh);
        news=input("Jam news • contoh 20:30");
        newsCard.addView(news);
        now=tv("Waktu WITA sekarang: --:--:--",11);
        now.setTextColor(MUTED);
        newsCard.addView(now);
        updateNow();
        root.addView(newsCard);

        LinearLayout market = card();
        heading(market,"04  KONFIRMASI MARKET","Konfirmasi inti sebelum entry.");
        liq=check("LIQUIDITY  •  wajib");
        cisd=check("CISD  •  wajib");
        bos=check("BOS  •  opsional");
        idm=check("IDM  •  opsional");
        market.addView(liq); market.addView(cisd); market.addView(bos); market.addView(idm);
        root.addView(market);

        LinearLayout plan = card();
        heading(plan,"05  ENTRY PLAN","Rencanakan entry sebelum MT5 dibuka.");

        TextView e1=tv("PLAN ENTRY",10); e1.setTextColor(MUTED); plan.addView(e1);
        entry=input("Contoh: Sell setelah CISD di POI H1"); plan.addView(entry);

        TextView e2=tv("TAKE PROFIT",10); e2.setTextColor(MUTED); plan.addView(e2);
        tp=input("Target harga / pip"); plan.addView(tp);

        TextView e3=tv("STOP LOSS",10); e3.setTextColor(MUTED); plan.addView(e3);
        sl=input("Invalidasi harga / pip"); plan.addView(sl);
        root.addView(plan);

        unlock=action("CEK & UNLOCK MT5");
        unlock.setTextSize(15);
        unlock.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        unlock.setTextColor(Color.rgb(30,24,28));
        unlock.setBackground(shape(PINK, PINK,0,16));
        LinearLayout.LayoutParams up=new LinearLayout.LayoutParams(-1,dp(56));
        up.setMargins(0,dp(3),0,dp(10));
        root.addView(unlock,up);
        unlock.setOnClickListener(v->checkUnlock());

        mt5=action("BUKA MT5");
        mt5.setEnabled(false);
        root.addView(mt5,new LinearLayout.LayoutParams(-1,dp(50)));
        mt5.setOnClickListener(v->open());

        Button lock=action("⚙  AKTIFKAN APP LOCK");
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(48));
        ap.topMargin=dp(10);
        root.addView(lock,ap);
        lock.setOnClickListener(v->{
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch(Exception e){ Toast.makeText(this,"Tidak bisa membuka pengaturan Accessibility.",Toast.LENGTH_LONG).show(); }
        });

        TextView info=tv("Aktifkan Trading Gate di Pengaturan > Aksesibilitas agar MT5 ikut ditahan saat Gate terkunci.",11);
        info.setTextColor(MUTED); info.setGravity(Gravity.CENTER);
        info.setPadding(dp(8),dp(9),dp(8),0);
        root.addView(info);

        scroll.addView(root);
        setContentView(scroll);
    }

    void updateNow() {
        ZonedDateTime z=ZonedDateTime.now(ZoneId.of("Asia/Makassar"));
        if(now!=null) now.setText("Waktu WITA sekarang: "+z.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    String validate() {
        if(busy.isChecked()) return "LOCK: kamu sedang sibuk.";
        if(tired.isChecked()) return "LOCK: kamu sedang capek / emosi.";
        if(!h4.isChecked()||!h2.isChecked()||!h1.isChecked()) return "LOCK: zona H4, H2, H1 harus jelas.";
        if(!liq.isChecked()) return "LOCK: Liquidity belum dikonfirmasi.";
        if(!cisd.isChecked()) return "LOCK: CISD belum dikonfirmasi.";
        if(!bos.isChecked()) return "LOCK: BOS belum dikonfirmasi.";
        if(!idm.isChecked()) return "LOCK: IDM belum dikonfirmasi.";
        if(entry.getText().toString().trim().isEmpty()) return "LOCK: Plan Entry wajib diisi.";
        if(tp.getText().toString().trim().isEmpty()) return "LOCK: TP wajib diisi.";
        if(sl.getText().toString().trim().isEmpty()) return "LOCK: SL wajib diisi.";

        String s=news.getText().toString().trim();
        if(!s.matches("\\d{2}:\\d{2}")) return "LOCK: isi jam News Besar dengan format HH:mm WITA.";

        int h,m;
        try { h=Integer.parseInt(s.substring(0,2)); m=Integer.parseInt(s.substring(3,5)); }
        catch(Exception e){ return "LOCK: format jam News tidak valid."; }
        if(h>23||m>59) return "LOCK: jam News tidak valid.";

        ZoneId wita=ZoneId.of("Asia/Makassar");
        ZonedDateTime current=ZonedDateTime.now(wita);
        LocalDateTime dt=LocalDateTime.of(current.toLocalDate(),LocalTime.of(h,m));
        ZonedDateTime newsTime=dt.atZone(wita);
        ZonedDateTime unlockTime=newsTime.minusHours(2);

        if(current.isBefore(unlockTime))
            return "LOCK: belum masuk window 2 jam sebelum news. Gate mulai "+
                    unlockTime.format(DateTimeFormatter.ofPattern("HH:mm"))+" WITA.";
        if(current.isAfter(newsTime)) return "LOCK: waktu News Besar sudah lewat.";
        return null;
    }

    void checkUnlock() {
        String e=validate();
        if(e!=null){
            status.setText("TERKUNCI"); status.setTextColor(RED);
            reason.setText(e); mt5.setEnabled(false); return;
        }

        p.edit().putBoolean("unlocked",true)
                .putLong("until",System.currentTimeMillis()+UNLOCK).apply();

        status.setText("TERBUKA"); status.setTextColor(GREEN);
        reason.setText("Checklist lolos. Gate aktif 15 menit.");
        mt5.setEnabled(true);
        unlock.setText("✓  GATE TERBUKA");
        startTimer();
    }

    void startTimer() {
        if(cd!=null) cd.cancel();
        long left=p.getLong("until",0)-System.currentTimeMillis();
        if(left<=0){ lock(); return; }

        cd=new CountDownTimer(left,1000){
            public void onTick(long ms){
                long sec=ms/1000;
                timer.setText(String.format(Locale.US,"MT5 terbuka  •  %02d:%02d",sec/60,sec%60));
            }
            public void onFinish(){ lock(); }
        }.start();
    }

    void lock() {
        if(cd!=null) cd.cancel();
        p.edit().putBoolean("unlocked",false).putLong("until",0).apply();
        status.setText("TERKUNCI"); status.setTextColor(RED);
        timer.setText(""); reason.setText("Waktu habis. Checklist harus diulang.");
        mt5.setEnabled(false); unlock.setText("CEK & UNLOCK MT5");

        h4.setChecked(false); h2.setChecked(false); h1.setChecked(false);
        liq.setChecked(false); cisd.setChecked(false); bos.setChecked(false); idm.setChecked(false);
        entry.setText(""); tp.setText(""); sl.setText("");
    }

    void restore() {
        boolean unlocked=p.getBoolean("unlocked",false);
        long until=p.getLong("until",0);

        if(unlocked && until>System.currentTimeMillis()){
            status.setText("TERBUKA"); status.setTextColor(GREEN);
            reason.setText("Gate masih aktif."); mt5.setEnabled(true);
            unlock.setText("✓  GATE TERBUKA"); startTimer();
        } else lock();
    }

    void open() {
        Intent i=getPackageManager().getLaunchIntentForPackage(MT5);
        if(i!=null) startActivity(i);
        else Toast.makeText(this,"MT5 tidak ditemukan di HP.",Toast.LENGTH_LONG).show();
    }
}
