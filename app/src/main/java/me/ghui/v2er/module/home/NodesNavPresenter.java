package me.ghui.v2er.module.home;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import me.ghui.v2er.general.App;
import me.ghui.v2er.network.APIService;
import me.ghui.v2er.network.GeneralConsumer;
import me.ghui.v2er.network.bean.NodesNavInfo;
import me.ghui.v2er.util.Check;

/**
 * Created by ghui on 22/05/2017.
 */

public class NodesNavPresenter implements NodesNavConstract.IPresenter {
    private static final long CACHE_VALID_TIME = 24 * 60 * 60 * 1000L;
    private static final String CACHE_FILE_NAME = "nodes_nav_planes.html";

    private NodesNavConstract.IView mView;

    public NodesNavPresenter(NodesNavConstract.IView view) {
        mView = view;
    }

    @Override
    public void start() {
        load(false);
    }

    @Override
    public void refresh() {
        load(true);
    }

    private void load(boolean forceRefresh) {
        Observable<NodesNavInfo> observable;
        String cachedHtml = forceRefresh ? null : readCachedHtml();
        if (Check.isEmpty(cachedHtml)) {
            observable = APIService.get().nodesNavHtml()
                    .map(response -> {
                        String html = response.string();
                        cacheHtml(html);
                        return parsePlanes(html);
                    });
        } else {
            observable = Observable.just(cachedHtml)
                    .map(this::parsePlanes);
        }

        observable
                .compose(mView.rx())
                .subscribe(new GeneralConsumer<NodesNavInfo>(mView) {
                    @Override
                    public void onConsume(NodesNavInfo items) {
                        mView.fillView(items);
                    }
                });
    }

    private String readCachedHtml() {
        File cacheFile = cacheFile();
        if (!cacheFile.exists()) return null;
        if (System.currentTimeMillis() - cacheFile.lastModified() > CACHE_VALID_TIME) return null;

        try (FileInputStream inputStream = new FileInputStream(cacheFile);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toString("UTF-8");
        } catch (IOException e) {
            return null;
        }
    }

    private void cacheHtml(String html) {
        if (Check.isEmpty(html)) return;
        try (FileOutputStream outputStream = new FileOutputStream(cacheFile())) {
            outputStream.write(html.getBytes("UTF-8"));
        } catch (IOException ignored) {
        }
    }

    private File cacheFile() {
        return new File(App.get().getCacheDir(), CACHE_FILE_NAME);
    }

    private NodesNavInfo parsePlanes(String html) {
        NodesNavInfo navInfo = new NodesNavInfo();
        navInfo.setResponse(html);

        Document document = Jsoup.parse(html);
        Elements boxes = document.select("div.box");
        for (Element box : boxes) {
            Elements nodeItems = box.select("a.item_node");
            if (nodeItems.isEmpty()) continue;

            Element header = box.select("div.header").first();
            String category = header == null ? "" : header.ownText().trim();
            if (category.length() == 0) continue;

            List<NodesNavInfo.Item.NodeItem> nodes = new ArrayList<>();
            for (Element node : nodeItems) {
                String name = node.text().trim();
                String link = node.attr("href").trim();
                if (name.length() == 0 || link.length() == 0) continue;
                nodes.add(new NodesNavInfo.Item.NodeItem(name, link));
            }
            if (!nodes.isEmpty()) {
                navInfo.add(new NodesNavInfo.Item(category, nodes));
            }
        }
        return navInfo;
    }

}
