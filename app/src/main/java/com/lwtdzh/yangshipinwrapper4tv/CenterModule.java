package com.lwtdzh.yangshipinwrapper4tv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CenterModule {
    private static final String TAG = "CenterModule";

    private final Activity activity;
    private final RuyiApi ruyiApi;
    private final Handler handler;
    private final FrameLayout root;

    private FrameLayout centerOverlay;
    private LinearLayout centerPanel;
    private TextView noticeText;
    private TextView accountText;
    private TextView vipBadge;
    private TextView expireText;
    private TextView macText;
    private ListView productListView;
    private ProductAdapter productAdapter;
    private List<RuyiApi.ProductInfo> products = new ArrayList<RuyiApi.ProductInfo>();
    private int productSelection = 0;

    private FrameLayout qrOverlay;
    private ImageView qrImageView;
    private TextView qrHintText;
    private String pendingPayUrl;
    private String pendingOrderNo;
    private String pendingPayWay = "wx";
    private int payPollCount = 0;
    private static final int MAX_POLL = 15;

    private final Runnable payPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (pendingOrderNo == null || payPollCount >= MAX_POLL) {
                qrHintText.setText(qrHintText.getText() + "\n查询超时，请按【OK】重试");
                return;
            }
            payPollCount++;
            ruyiApi.queryPayResult(pendingOrderNo, new RuyiApi.PayQueryCallback() {
                @Override
                public void onPayState(int state, String msg) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!qrVisible()) return;
                            if (state == 2) {
                                qrHintText.setText("✅ 支付成功！VIP 已到账\n按【← 返回】关闭");
                                if (activity != null) {
                                    android.content.Intent intent = new android.content.Intent("com.lwtdzh.action.REFRESH_VIP");
                                    activity.sendBroadcast(intent);
                                }
                            } else if (state == 0) {
                                qrHintText.setText(qrHintText.getText() + "\n查询中... (" + payPollCount + "/" + MAX_POLL + ")");
                                handler.postDelayed(payPollRunnable, 3000);
                            } else {
                                qrHintText.setText("⚠️ " + msg + "\n请重新扫码或按【OK】重试");
                                handler.postDelayed(payPollRunnable, 3000);
                            }
                        }
                    });
                }
            });
        }
    };

    private boolean visible = false;
    private boolean autoCloseOnChannelChange = true;

    public CenterModule(Activity act, RuyiApi api, FrameLayout rootLayout) {
        this.activity = act;
        this.ruyiApi = api;
        this.root = rootLayout;
        this.handler = new Handler(Looper.getMainLooper());
        build();
    }

    private void build() {
        centerOverlay = new FrameLayout(activity);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        centerOverlay.setLayoutParams(lp);

        centerPanel = new LinearLayout(activity);
        centerPanel.setOrientation(LinearLayout.VERTICAL);
        centerPanel.setBackgroundColor(0xE6000000);
        centerPanel.setPadding(dp(24), dp(20), dp(24), dp(20));
        LinearLayout.LayoutParams panelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        centerPanel.setLayoutParams(panelParams);

        buildHeader();
        buildNoticeBar();
        buildDivider();
        buildAccountSection();
        buildDivider();
        buildProductsSection();

        buildQrOverlay();

        centerOverlay.addView(centerPanel);
        centerOverlay.addView(qrOverlay);
        centerOverlay.setVisibility(View.GONE);

        root.addView(centerOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.RIGHT));
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        header.setLayoutParams(hlp);

        TextView title = new TextView(activity);
        title.setText("👤 个人中心");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(tlp);
        header.addView(title);

        TextView closeHint = new TextView(activity);
        closeHint.setText("◀ 左键关闭 | OK 充值");
        closeHint.setTextColor(0xFF888888);
        closeHint.setTextSize(13);
        header.addView(closeHint);

        centerPanel.addView(header);
    }

    private void buildNoticeBar() {
        noticeText = new TextView(activity);
        noticeText.setTextColor(0xFFFFC107);
        noticeText.setTextSize(13);
        noticeText.setSingleLine(true);
        noticeText.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
        noticeText.setMarqueeRepeatLimit(-1);
        noticeText.setSelected(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(6);
        noticeText.setLayoutParams(lp);
        centerPanel.addView(noticeText);
    }

    private void buildQrOverlay() {
        qrOverlay = new FrameLayout(activity);
        FrameLayout.LayoutParams olp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        qrOverlay.setLayoutParams(olp);
        qrOverlay.setBackgroundColor(0xCC000000);
        qrOverlay.setVisibility(View.GONE);

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        container.setLayoutParams(clp);

        TextView title = new TextView(activity);
        title.setText("📱 扫码支付");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        title.setLayoutParams(tlp);
        container.addView(title);

        qrImageView = new ImageView(activity);
        int qrSize = dp(280);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(qrSize, qrSize);
        ilp.topMargin = dp(20);
        qrImageView.setLayoutParams(ilp);
        qrImageView.setBackgroundColor(Color.WHITE);
        container.addView(qrImageView);

        qrHintText = new TextView(activity);
        qrHintText.setText("请用手机微信/支付宝扫描二维码\n按【OK】刷新 | 按【← 返回】关闭");
        qrHintText.setTextColor(0xFFCCCCCC);
        qrHintText.setTextSize(14);
        qrHintText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = dp(16);
        qrHintText.setLayoutParams(hlp);
        container.addView(qrHintText);

        qrOverlay.addView(container);
    }

    private void buildDivider() {
        View v = new View(activity);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dlp.topMargin = dp(12);
        dlp.bottomMargin = dp(12);
        v.setLayoutParams(dlp);
        v.setBackgroundColor(0x33FFFFFF);
        centerPanel.addView(v);
    }

    private void buildAccountSection() {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        section.setLayoutParams(slp);

        LinearLayout row1 = new LinearLayout(activity);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        row1.setLayoutParams(rlp);

        TextView accLabel = new TextView(activity);
        accLabel.setText("账号：");
        accLabel.setTextColor(0xFFAAAAAA);
        accLabel.setTextSize(15);
        row1.addView(accLabel);

        accountText = new TextView(activity);
        accountText.setTextColor(Color.WHITE);
        accountText.setTextSize(16);
        accountText.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams atl = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        accountText.setLayoutParams(atl);
        row1.addView(accountText);

        vipBadge = new TextView(activity);
        vipBadge.setTextSize(12);
        vipBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
        vipBadge.setTextColor(Color.WHITE);
        row1.addView(vipBadge);

        section.addView(row1);

        expireText = new TextView(activity);
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        elp.topMargin = dp(8);
        expireText.setLayoutParams(elp);
        expireText.setTextColor(0xFFAAAAAA);
        expireText.setTextSize(14);
        section.addView(expireText);

        macText = new TextView(activity);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = dp(6);
        macText.setLayoutParams(mlp);
        macText.setTextColor(0xFF888888);
        macText.setTextSize(13);
        section.addView(macText);

        centerPanel.addView(section);
    }

    private void buildProductsSection() {
        TextView sectionTitle = new TextView(activity);
        sectionTitle.setText("💎 VIP 套餐");
        sectionTitle.setTextColor(Color.WHITE);
        sectionTitle.setTextSize(16);
        sectionTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams stlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        stlp.topMargin = dp(8);
        sectionTitle.setLayoutParams(stlp);
        centerPanel.addView(sectionTitle);

        productListView = new ListView(activity);
        productListView.setDivider(new android.graphics.drawable.ColorDrawable(0x22FFFFFF));
        productListView.setDividerHeight(1);
        productListView.setCacheColorHint(Color.TRANSPARENT);
        productListView.setSelector(new android.graphics.drawable.ColorDrawable(0x33FFFFFF));
        productAdapter = new ProductAdapter();
        productListView.setAdapter(productAdapter);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        plp.topMargin = dp(8);
        centerPanel.addView(productListView, plp);
    }

    public void show() {
        if (visible) return;
        visible = true;
        refreshAccountInfo();
        fetchRemoteProducts();
        fetchNotice();
        productSelection = 0;
        centerOverlay.setVisibility(View.VISIBLE);
        Log.i(TAG, "center_show");
    }

    public void hide() {
        visible = false;
        hideQrDialog();
        centerOverlay.setVisibility(View.GONE);
        Log.i(TAG, "center_hide");
    }

    public void toggle() {
        if (visible) hide(); else show();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setAutoCloseOnChannelChange(boolean enable) {
        this.autoCloseOnChannelChange = enable;
    }

    public void onChannelChanged() {
        if (autoCloseOnChannelChange && visible) {
            hide();
        }
    }

    private void refreshAccountInfo() {
        String user = ruyiApi.getUsername();
        if (user == null || user.isEmpty()) user = "未登录";
        accountText.setText(user);

        boolean isVip = ruyiApi.isVip();
        if (isVip) {
            vipBadge.setText("VIP");
            vipBadge.setBackgroundColor(0xFFE6A23C);
        } else {
            vipBadge.setText("普通用户");
            vipBadge.setBackgroundColor(0xFF666666);
        }

        long vipTime = ruyiApi.getVipTime();
        if (vipTime == 999999999L) {
            expireText.setText("到期时间：永久");
        } else if (vipTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(new Date(vipTime * 1000L));
            expireText.setText("到期时间：" + date);
        } else {
            expireText.setText("到期时间：未开通");
        }

        String[] macs = getMacAddresses();
        Log.i(TAG, "mac_result eth=" + macs[0] + " wifi=" + macs[1]);
        StringBuilder macSb = new StringBuilder();
        if (macs[0] != null) macSb.append("有线：").append(macs[0]);
        if (macs[1] != null) {
            if (macSb.length() > 0) macSb.append("  ");
            macSb.append("无线：").append(macs[1]);
        }
        macText.setText(macSb.length() > 0 ? macSb.toString() : "MAC：未知");
    }

    private String[] getMacAddresses() {
        String ethMac = null;
        String wifiMac = null;

        try {
            android.content.Context ctx = activity.getApplicationContext();
            android.net.wifi.WifiManager wm = (android.net.wifi.WifiManager) ctx.getSystemService(android.content.Context.WIFI_SERVICE);
            if (wm != null) {
                android.net.wifi.WifiInfo wi = wm.getConnectionInfo();
                if (wi != null) {
                    String m = wi.getMacAddress();
                    if (m != null && !m.isEmpty() && !m.equals("02:00:00:00:00:00") && !m.equals("00:00:00:00:00:00")) {
                        wifiMac = m.toUpperCase();
                    }
                    Log.i(TAG, "wifiManager_mac=" + m);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "wifiManager error: " + e.getMessage());
        }

        if (ethMac == null) {
            String[] ethCandidates = { "eth0", "eth1", "bond0", "bond1", "lan0", "usb0" };
            for (String iface : ethCandidates) {
                String m = readSysMac(iface);
                if (m != null) { ethMac = m; break; }
            }
        }
        if (wifiMac == null) {
            String[] wifiCandidates = { "wlan0", "wlan1", "wifi0" };
            for (String iface : wifiCandidates) {
                String m = readSysMac(iface);
                if (m != null) { wifiMac = m; break; }
            }
        }

        if (ethMac == null && wifiMac == null) {
            try {
                java.util.Enumeration<java.net.NetworkInterface> nis = java.net.NetworkInterface.getNetworkInterfaces();
                while (nis.hasMoreElements()) {
                    java.net.NetworkInterface ni = nis.nextElement();
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null && mac.length == 6) {
                        String s = bytesToMac(mac);
                        if (s.startsWith("00:00:00") || s.startsWith("02:00:00")) continue;
                        String name = ni.getName().toLowerCase();
                        if ((name.startsWith("eth") || name.startsWith("bond") || name.startsWith("lan")) && ethMac == null) {
                            ethMac = s;
                        } else if ((name.startsWith("wlan") || name.startsWith("wifi")) && wifiMac == null) {
                            wifiMac = s;
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "fallback netif error: " + e.getMessage());
            }
        }

        Log.i(TAG, "mac_final eth=" + ethMac + " wifi=" + wifiMac);
        return new String[]{ ethMac, wifiMac };
    }

    private static String readSysMac(String iface) {
        try {
            java.io.File f = new java.io.File("/sys/class/net/" + iface + "/address");
            if (!f.exists()) return null;
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(f));
            String line = r.readLine();
            r.close();
            if (line != null && line.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}")) {
                return line.toUpperCase();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String bytesToMac(byte[] mac) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(':');
            sb.append(String.format("%02X", mac[i] & 0xFF));
        }
        return sb.toString();
    }

    private void fetchRemoteProducts() {
        ruyiApi.fetchProducts(new RuyiApi.ProductsCallback() {
            @Override
            public void onProductsResult(List<RuyiApi.ProductInfo> list) {
                products = list;
                Log.i(TAG, "products_loaded count=" + list.size());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        productAdapter.notifyDataSetChanged();
                        if (productSelection >= products.size() && !products.isEmpty()) {
                            productSelection = 0;
                        }
                        productListView.setSelection(productSelection);
                    }
                });
            }
        });
    }

    private void fetchNotice() {
        noticeText.setText("正在加载公告...");
        ruyiApi.checkVersion("0.0.0", new RuyiApi.VersionCallback() {
            @Override
            public void onVersionResult(RuyiApi.VersionInfo info) {
                Log.i(TAG, "notice_loaded updateNotes=" + info.updateNotes);
                String note = info.updateNotes;
                if (note != null && note.length() > 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            noticeText.setText("📢 公告：" + note);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            noticeText.setText("📢 欢迎使用 YSPTV 个人中心");
                        }
                    });
                }
            }
        });
    }

    private boolean qrVisible() {
        return qrOverlay != null && qrOverlay.getVisibility() == View.VISIBLE;
    }

    private void showQrDialog(RuyiApi.ProductInfo p) {
        pendingPayUrl = RuyiApi.BASE_URL + "/pay.php?product_id=" + p.id + "&product_name=" +
                android.net.Uri.encode(p.name) + "&price=" + p.price +
                "&token=" + (ruyiApi.getToken() != null ? ruyiApi.getToken() : "");
        qrHintText.setText("产品：" + p.name + "（¥" + p.price + "）\n请用手机微信/支付宝扫码支付\n按【OK】刷新 | 按【← 返回】关闭");
        qrImageView.setImageDrawable(null);
        qrOverlay.setVisibility(View.VISIBLE);
        loadQrBitmap(pendingPayUrl);
        Log.i(TAG, "show_qr product=" + p.name + " url=" + pendingPayUrl);
    }

    private void hideQrDialog() {
        qrOverlay.setVisibility(View.GONE);
        pendingPayUrl = null;
    }

    private void loadQrBitmap(final String dataUrl) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=" +
                                    java.net.URLEncoder.encode(dataUrl, "UTF-8");
                            java.net.URL u = new java.net.URL(url);
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
                            conn.setConnectTimeout(8000);
                            conn.setReadTimeout(8000);
                            java.io.InputStream is = conn.getInputStream();
                            final Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
                            is.close();
                            conn.disconnect();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (bmp != null && qrVisible()) {
                                        qrImageView.setImageBitmap(bmp);
                                    } else if (bmp == null) {
                                        qrHintText.setText("二维码加载失败\n请检查网络后按【OK】重试");
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "loadQrBitmap error: " + e.getMessage());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    qrHintText.setText("二维码加载失败\n请检查网络后按【OK】重试");
                                }
                            });
                        }
                    }
                }).start();
            }
        }, 50);
    }

    public boolean handleKey(int keyCode) {
        if (!visible) return false;

        if (qrVisible()) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                hideQrDialog();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (pendingPayUrl != null) {
                    qrHintText.setText("正在刷新二维码...");
                    loadQrBitmap(pendingPayUrl);
                }
                return true;
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            hide();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (productSelection > 0) {
                productSelection--;
                productListView.setSelection(productSelection);
                productAdapter.notifyDataSetChanged();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (productSelection < products.size() - 1) {
                productSelection++;
                productListView.setSelection(productSelection);
                productAdapter.notifyDataSetChanged();
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            openPayPage();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            hide();
            return true;
        }

        return true;
    }

    private void openPayPage() {
        if (products.isEmpty()) return;
        if (productSelection >= products.size()) return;
        RuyiApi.ProductInfo p = products.get(productSelection);
        Log.i(TAG, "open_pay product=" + p.name);
        showQrDialog(p);
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
        if (centerOverlay != null && root != null) {
            root.removeView(centerOverlay);
        }
        centerOverlay = null;
    }

    private int dp(int px) {
        float density = activity.getResources().getDisplayMetrics().density;
        return (int) (px * density + 0.5f);
    }

    private class ProductAdapter extends BaseAdapter {
        @Override
        public int getCount() { return products.size(); }

        @Override
        public Object getItem(int position) { return products.get(position); }

        @Override
        public long getItemId(int position) { try { return Long.parseLong(products.get(position).id); } catch (Exception e) { return position; } }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(14), dp(12), dp(14));

            boolean selected = position == productSelection;
            if (selected) {
                row.setBackgroundColor(0xFF333333);
            }

            TextView nameTv = new TextView(activity);
            nameTv.setText(products.get(position).name);
            nameTv.setTextSize(16);
            nameTv.setTextColor(Color.WHITE);
            if (selected) nameTv.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameTv.setLayoutParams(nlp);
            row.addView(nameTv);

            String suffix = "";
            if (products.get(position).duration != null && !products.get(position).duration.isEmpty()) {
                suffix = " · " + products.get(position).duration;
            }
            TextView priceTv = new TextView(activity);
            priceTv.setText("¥" + products.get(position).price + suffix);
            priceTv.setTextSize(15);
            priceTv.setTextColor(selected ? 0xFFFFD700 : 0xFFCCCCCC);
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            priceTv.setLayoutParams(plp);
            row.addView(priceTv);

            return row;
        }
    }
}