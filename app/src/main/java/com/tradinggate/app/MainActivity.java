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
