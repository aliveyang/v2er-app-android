package me.ghui.v2er.network.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.ghui.fruit.Attrs;
import me.ghui.fruit.annotations.Pick;
import me.ghui.v2er.util.Check;

/**
 * Created by ghui on 21/05/2017.
 * https://www.v2ex.com/planes
 */

@Pick("div.box")
public class NodesNavInfo extends ArrayList<NodesNavInfo.Item> implements IBase {
    private String mResponseBody;

    @Override
    public String getResponse() {
        return mResponseBody;
    }

    @Override
    public void setResponse(String response) {
        mResponseBody = response;
    }

    @Override
    public boolean isValid() {
        Iterator<Item> iterator = iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (item == null || Check.isEmpty(item.category) || Check.isEmpty(item.nodes)) {
                iterator.remove();
            }
        }
        return size() > 0;
    }

    public static class Item implements Serializable {
        @Pick(value = "div.header", attr = Attrs.OWN_TEXT)
        private String category;
        @Pick("a.item_node")
        private List<NodeItem> nodes;

        public Item() {
        }

        public Item(String category, List<NodeItem> nodes) {
            this.category = category;
            this.nodes = nodes;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "category='" + category + '\'' +
                    ", nodes=" + nodes +
                    '}';
        }

        public String getCategory() {
            return category;
        }

        public List<NodeItem> getNodes() {
            return nodes;
        }

        public static class NodeItem implements Serializable{
            @Pick
            private String name;
            @Pick(attr = Attrs.HREF)
            private String link;

            public NodeItem() {
            }

            public NodeItem(String name, String link) {
                this.name = name;
                this.link = link;
            }

            @Override
            public String toString() {
                return "NodeItem{" +
                        "name='" + name + '\'' +
                        ", link='" + link + '\'' +
                        '}';
            }

            public String getName() {
                return name;
            }

            public String getLink() {
                return link;
            }
        }
    }

}
