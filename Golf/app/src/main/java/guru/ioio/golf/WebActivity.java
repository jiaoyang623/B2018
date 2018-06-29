package guru.ioio.golf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.store.ScriptStoreSQLite;
import guru.ioio.golf.databinding.ActivityWebBinding;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WebActivity extends Activity {

    private static final String SCRIPT_URL = "https://greasyfork.org/scripts/39776-%E7%99%BE%E5%BA%A6%E7%BD%91%E7%9B%98%E7%9B%B4%E6%8E%A5%E4%B8%8B%E8%BD%BD%E5%8A%A9%E6%89%8B%E4%BF%AE%E6%94%B9%E7%89%88/code/%E7%99%BE%E5%BA%A6%E7%BD%91%E7%9B%98%E7%9B%B4%E6%8E%A5%E4%B8%8B%E8%BD%BD%E5%8A%A9%E6%89%8B%E4%BF%AE%E6%94%B9%E7%89%88.user.js";
    private ActivityWebBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_web);
        initWebView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scriptStore != null) {
            scriptStore.close();
        }
    }

    private void loadUrl() {
        mBinding.web.loadUrl("https://pan.baidu.com/s/1dGn8W17#list/path=%2F%E6%88%91%E7%9A%84%E8%B5%84%E6%BA%90%2F%E9%9F%A9%20%20%E5%89%A7%2F%E8%87%B4%E5%BF%98%E4%BA%86%E8%AF%97%E7%9A%84%E4%BD%A0.2018.%E5%90%8C%E6%AD%A5%E8%BF%9E%E8%BD%BD&parentPath=%2F%E6%88%91%E7%9A%84%E8%B5%84%E6%BA%90");
    }


    public boolean loadJs() {
        return true;
    }

    private ScriptStoreSQLite scriptStore;

    @SuppressLint("CheckResult")
    private void loadScripts() {
        Observable.create(s -> {
            if (scriptStore == null) {
                scriptStore = new ScriptStoreSQLite(this);
                scriptStore.open();
                Script[] scripts = scriptStore.get(SCRIPT_URL);
                if (scripts == null || scripts.length == 0) {
                    try (InputStream in = getAssets().open("bdwp.js")) {
                        String data = IOUtils.toString(in);
                        Script script = Script.parse(data, SCRIPT_URL);
                        if (script != null) {
                            scriptStore.add(script);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            s.onNext(scriptStore);
        }).subscribeOn(Schedulers.io()).map(o -> (ScriptStoreSQLite) o)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(scriptStore -> {
            mBinding.web.setScriptStore(scriptStore);
            mBinding.web.setWebViewClient(new WebViewClientGm(scriptStore,
                    mBinding.web.getWebViewClient().getJsBridgeName(), mBinding.web
                    .getWebViewClient().getSecret()));
            loadUrl();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//        }

        loadScripts();

        mBinding.progress.setMax(100);
        mBinding.web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mBinding.progress.setVisibility(newProgress > 0 && newProgress < 100 ? View.VISIBLE : View.INVISIBLE);
                mBinding.progress.setProgress(newProgress);
            }
        });

//        mBinding.web.setWebViewClient(new WebViewClient());

        WebSettings settings = mBinding.web.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //支持js
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
        //自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        //自动缩放
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        settings.setDisplayZoomControls(true);
        settings.setAllowFileAccess(true); // 允许访问文件
        mBinding.web.setInitialScale(10);

    }
}
