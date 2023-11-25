import java.util.LinkedList;
import java.util.List;

public class TreeNode {
    private String content;
    private List<TreeNode> childNodes;
    private TreeNode parent = null;
    private Boolean truthValue = null;

    public TreeNode(String content) {
        this.content = content;
        this.childNodes = new LinkedList<>();
    }

    public void addChild(TreeNode childNode) {
        childNode.setParent(this);
        this.childNodes.add(childNode);
    }

    public String getContent() {
        return content;
    }

    public List<TreeNode> getChildNodes() {
        return childNodes;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setTruthValue(Boolean truthValue) {
        this.truthValue = truthValue;
    }

    public Boolean getTruthValue() {
        return truthValue;
    }
}

