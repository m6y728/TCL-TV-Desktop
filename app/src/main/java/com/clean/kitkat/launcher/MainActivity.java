package com.clean.kitkat.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private static final int DOCK_COUNT = 5;
    private static final int VISIBLE_ROW_CARDS = 5;
    private static final int APP_CARD_WIDTH_DP = 180;
    private static final int APP_CARD_GAP_DP = 24;
    private static final String TAG = "KitkatLauncher";

    private static final String[] FAVORITE_PACKAGES = {
            "com.tcl.appmarket2",
            "com.mygithub0.tvbox0.osdkitkat",
            "com.tcl.tv",
            "com.tcl.tvweishi"
    };

    private final List<AppEntry> apps = new ArrayList<AppEntry>();
    private final List<View> dockTiles = new ArrayList<View>();
    private final List<View> rowTiles = new ArrayList<View>();
    private int rowCardWidth;

    private TextView heroName;
    private View ambient;
    private StepScrollView appsScroll;
    private LinearLayout appsRow;
    private View wifiPill;
    private View settingsPill;
    private View sourcePill;
    private String launchedPackage;

    private static final int[] WALLPAPERS = {
            R.drawable.wallpaper_1, R.drawable.wallpaper_2, R.drawable.wallpaper_3,
            R.drawable.wallpaper_4, R.drawable.wallpaper_5, R.drawable.wallpaper_6,
            R.drawable.wallpaper_7
    };
    private static final long WALLPAPER_INTERVAL_MS = 30000L;

    private ImageView wallpaper;
    private final Handler wallpaperHandler = new Handler();
    private Bitmap wallpaperBitmap;
    private int wallpaperIndex;
    private final Runnable wallpaperRunnable = new Runnable() {
        @Override
        public void run() {
            wallpaperIndex = (wallpaperIndex + 1) % WALLPAPERS.length;
            showWallpaper(wallpaperIndex);
            wallpaperHandler.postDelayed(this, WALLPAPER_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        heroName = (TextView) findViewById(R.id.hero_name);
        ambient = findViewById(R.id.ambient);
        appsScroll = (StepScrollView) findViewById(R.id.apps_scroll);
        appsRow = (LinearLayout) findViewById(R.id.apps_row);
        appsRow.setClipChildren(true);
        appsRow.setClipToPadding(true);
        wallpaper = (ImageView) findViewById(R.id.wallpaper);
        showWallpaper(0);

        setHeader();
        setupPills();
        loadApps();

        setupAppsRow();
        LinearLayout dock = (LinearLayout) findViewById(R.id.dock);
        setupDock(dock);

        if (!apps.isEmpty()) {
            heroName.setText(displayLabel(apps.get(0)));
        }
        setupAmbient();

        focusFirst();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopBackgroundPlayback();
        focusFirst();
        wallpaperHandler.removeCallbacks(wallpaperRunnable);
        wallpaperHandler.postDelayed(wallpaperRunnable, WALLPAPER_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        wallpaperHandler.removeCallbacks(wallpaperRunnable);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            focusFirst();
        }
    }

    private void focusFirst() {
        appsScroll.scrollTo(0, 0);
        appsRow.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }
                View first = !rowTiles.isEmpty() ? rowTiles.get(0)
                        : !dockTiles.isEmpty() ? dockTiles.get(0) : null;
                if (first != null) {
                    first.requestFocus();
                    if (rowTiles.contains(first)) {
                        appsScroll.snapToCard(0);
                    }
                }
            }
        }, 150);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused != null) {
                View target = null;
                if (rowTiles.contains(focused)) {
                    target = rowTarget(rowTiles.indexOf(focused), event.getKeyCode());
                } else if (dockTiles.contains(focused)) {
                    target = dockTarget(dockTiles.indexOf(focused), event.getKeyCode());
                } else if (focused == wifiPill || focused == settingsPill || focused == sourcePill) {
                    target = pillTarget(focused, event.getKeyCode());
                }
                if (target != null) {
                    target.requestFocus();
                    if (rowTiles.contains(target)) {
                        appsScroll.snapToCard(rowTiles.indexOf(target));
                    }
                    return true;
                }
                if (isDirectionKey(event.getKeyCode())
                        && (rowTiles.contains(focused) || dockTiles.contains(focused)
                                || focused == wifiPill || focused == settingsPill
                                || focused == sourcePill)) {
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private static boolean isDirectionKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN;
    }

    private View rowTarget(int index, int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return index > 0 ? rowTiles.get(index - 1) : null;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return index < rowTiles.size() - 1 ? rowTiles.get(index + 1) : null;
            case KeyEvent.KEYCODE_DPAD_UP:
                return wifiPill;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (dockTiles.isEmpty()) {
                    return null;
                }
                return dockTiles.get(Math.min(index, dockTiles.size() - 1));
            default:
                return null;
        }
    }

    private View dockTarget(int index, int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return index > 0 ? dockTiles.get(index - 1) : null;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return index < dockTiles.size() - 1 ? dockTiles.get(index + 1) : null;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!rowTiles.isEmpty()) {
                    return rowTiles.get(Math.min(index, rowTiles.size() - 1));
                }
                return wifiPill;
            default:
                return null;
        }
    }

    private View pillTarget(View pill, int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (!rowTiles.isEmpty()) {
                return rowTiles.get(0);
            }
            return null;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (pill == wifiPill) {
                return settingsPill;
            }
            if (pill == settingsPill) {
                return sourcePill;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (pill == sourcePill) {
                return settingsPill;
            }
            if (pill == settingsPill) {
                return wifiPill;
            }
        }
        return null;
    }

    private void stopBackgroundPlayback() {
        if (launchedPackage == null) {
            return;
        }
        String pkg = launchedPackage;
        launchedPackage = null;
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            am.killBackgroundProcesses(pkg);
        } catch (Exception e) {
            Log.e(TAG, "killBackgroundProcesses failed", e);
        }
    }

    private void setHeader() {
        TextView greeting = (TextView) findViewById(R.id.greeting);
        TextView date = (TextView) findViewById(R.id.date);
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            greeting.setText(R.string.greeting_morning);
        } else if (hour >= 12 && hour < 18) {
            greeting.setText(R.string.greeting_afternoon);
        } else {
            greeting.setText(R.string.greeting_evening);
        }

        String weekdays = getString(R.string.weekdays);
        char weekday = weekdays.charAt(now.get(Calendar.DAY_OF_WEEK) - 1);
        date.setText(getString(R.string.date_format,
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH),
                String.valueOf(weekday)));
    }

    private void setupPills() {
        wifiPill = findViewById(R.id.wifi_pill);
        ((TextView) wifiPill).setText(wifiName());
        wifiPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNetworkSettings("Wifi");
            }
        });

        settingsPill = findViewById(R.id.settings_pill);
        settingsPill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsMain();
            }
        });

        sourcePill = findViewById(R.id.source_pill);
        sourcePill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInputSource();
            }
        });
    }

    private void openInputSource() {
        try {
            Intent intent = new Intent("com.tcl.insrc.SHOW_WINDOW")
                    .setClassName("com.tcl.sourcemananger", "com.tcl.insrc.ShowWindowService");
            startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "openInputSource failed", e);
        }
    }

    private void openNetworkSettings(String nettype) {
        try {
            Intent intent = new Intent("com.tcl.settings.SHOW_WINDOW")
                    .setClassName("com.tcl.settings", "com.tcl.settings.ShowWindowService")
                    .putExtra("flag", 1)
                    .putExtra("Catagory", "network")
                    .putExtra("nettype", nettype)
                    .putExtra("Type", "Settings");
            startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "openNetworkSettings failed", e);
        }
    }

    private void openSettingsMain() {
        try {
            Intent intent = new Intent("com.tcl.settings.SHOW_WINDOW")
                    .setClassName("com.tcl.settings", "com.tcl.settings.ShowWindowService")
                    .putExtra("flag", 1)
                    .putExtra("Type", "Settings");
            startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "openSettingsMain failed", e);
        }
    }

    private String wifiName() {
        try {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wm.getConnectionInfo();
            String ssid = info.getSSID();
            if (ssid == null || ssid.length() == 0 || ssid.equals("0x")
                    || ssid.equals("<unknown ssid>")) {
                return getString(R.string.wifi);
            }
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            return ssid;
        } catch (Exception e) {
            return getString(R.string.wifi);
        }
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = pm.queryIntentActivities(main, 0);
        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null) {
                continue;
            }
            if (info.activityInfo.packageName.equals(getPackageName())) {
                continue;
            }
            AppEntry entry = new AppEntry();
            entry.packageName = info.activityInfo.packageName;
            entry.label = info.loadLabel(pm).toString();
            if ("com.tcl.tv".equals(info.activityInfo.packageName)) {
                entry.icon = getResources().getDrawable(R.drawable.tv_cctv);
            } else if ("tv.bili".equals(info.activityInfo.packageName)) {
                entry.icon = getResources().getDrawable(R.drawable.bili_icon);
            } else {
                entry.icon = info.loadIcon(pm);
            }
            entry.intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            apps.add(entry);
        }
        Collections.sort(apps, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry a, AppEntry b) {
                return a.label.compareToIgnoreCase(b.label);
            }
        });
    }

    private void setupAppsRow() {
        appsRow.removeAllViews();
        rowTiles.clear();
        float density = getResources().getDisplayMetrics().density;
        int cardWidth = (int) (APP_CARD_WIDTH_DP * density);
        rowCardWidth = cardWidth;
        int gap = (int) (APP_CARD_GAP_DP * density);
        int slotSize = cardWidth + gap;

        List<AppEntry> rowApps = new ArrayList<AppEntry>();
        for (String packageName : FAVORITE_PACKAGES) {
            AppEntry entry = findApp(packageName);
            if (entry != null) {
                rowApps.add(entry);
            }
        }

        for (int i = DOCK_COUNT; i < apps.size(); i++) {
            AppEntry entry = apps.get(i);
            if (isFavorite(entry.packageName)) {
                continue;
            }
            rowApps.add(entry);
        }

        appsScroll.setSlotSize(slotSize);
        appsScroll.setVisibleSlots(VISIBLE_ROW_CARDS);
        appsScroll.setCardCount(rowApps.size());
        LinearLayout.LayoutParams scrollParams = (LinearLayout.LayoutParams) appsScroll.getLayoutParams();
        scrollParams.width = VISIBLE_ROW_CARDS * cardWidth + (VISIBLE_ROW_CARDS - 1) * gap;
        appsScroll.setLayoutParams(scrollParams);

        for (int i = 0; i < rowApps.size(); i++) {
            int rightMargin = i < rowApps.size() - 1 ? gap : 0;
            addRowCard(rowApps.get(i), 0, rightMargin);
        }
    }

    private void addRowCard(final AppEntry entry, int leftMargin, int rightMargin) {
        View tile = LayoutInflater.from(this).inflate(R.layout.item_app, appsRow, false);
        ImageView icon = (ImageView) tile.findViewById(R.id.icon);
        final TextView name = (TextView) tile.findViewById(R.id.name);
        View card = tile.findViewById(R.id.card);
        card.setBackgroundResource(R.drawable.dock_tile_selector);
        icon.setImageDrawable(entry.icon);
        name.setText(displayLabel(entry));
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launch(entry);
            }
        });
        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                name.setAlpha(hasFocus ? 1.0f : 0.55f);
                if (hasFocus) {
                    heroName.setText(displayLabel(entry));
                }
            }
        });
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tile.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        params.width = rowCardWidth;
        params.leftMargin = leftMargin;
        params.rightMargin = rightMargin;
        appsRow.addView(tile, params);
        rowTiles.add(card);
    }

    private void setupDock(LinearLayout dock) {
        dockTiles.clear();
        int count = Math.min(DOCK_COUNT, apps.size());
        for (int i = 0; i < count; i++) {
            final AppEntry entry = apps.get(i);
            View cell = LayoutInflater.from(this).inflate(R.layout.item_dock, dock, false);
            ImageView icon = (ImageView) cell.findViewById(R.id.icon);
            icon.setImageDrawable(entry.icon);
            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launch(entry);
                }
            });
            cell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        heroName.setText(displayLabel(entry));
                    }
                }
            });
            dockTiles.add(cell);
            dock.addView(cell);
        }
    }

    private AppEntry findApp(String packageName) {
        for (AppEntry entry : apps) {
            if (packageName.equals(entry.packageName)) {
                return entry;
            }
        }
        return null;
    }

    private String displayLabel(AppEntry entry) {
        if ("tv.bili".equals(entry.packageName)) {
            return "云视听小电视";
        }
        return entry.label;
    }

    private boolean isFavorite(String packageName) {
        for (String pkg : FAVORITE_PACKAGES) {
            if (pkg.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setupAmbient() {
        GradientDrawable glow = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] { Color.argb(80, 255, 255, 255), Color.argb(0, 255, 255, 255) });
        ambient.setBackground(glow);
    }

    private void showWallpaper(int index) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap next = BitmapFactory.decodeResource(getResources(), WALLPAPERS[index], opts);
        if (next == null) {
            return;
        }
        wallpaper.setImageBitmap(next);
        if (wallpaperBitmap != null && wallpaperBitmap != next) {
            wallpaperBitmap.recycle();
        }
        wallpaperBitmap = next;
    }

    private void launch(AppEntry entry) {
        if (entry.intent != null && entry.intent.getComponent() != null) {
            launchedPackage = entry.intent.getComponent().getPackageName();
        }
        launch(entry.intent);
    }

    private void launch(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "launch failed", e);
        }
    }

    @Override
    public void onBackPressed() {
        // Home screen: Back goes nowhere.
    }

    private static final class AppEntry {
        String label;
        String packageName;
        Drawable icon;
        Intent intent;
    }
}
