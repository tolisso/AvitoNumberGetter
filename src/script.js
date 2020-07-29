let node = document.evaluate("//button[contains(text(), \'Показать телефон\')]",
        document,
        null,
        XPathResult.FIRST_ORDERED_NODE_TYPE,
        null).singleNodeValue;

if (node === null) return null;
node.textContent = "scammed";
let result = [];
function chose_by_nodeName(node_array, name) {
    let ans = [];
    for (let node of node_array) {
        if (node.nodeName === name) {
            ans.push(node);
        }
    }
    return ans;
}
function find_son(father, son) {
    const children = chose_by_nodeName(father.childNodes, son.nodeName);
    for (let i = 0; i < children.length; i++) {
        if (children[i] === son) {
            return i;
        }
    }
}
while (true) {
    if (node.nodeName === "HTML") {
        result.push("html");
        break;
    }
    result.push(node.nodeName.toLowerCase());
    result[result.length - 1] = result[result.length - 1] + "[" + (find_son(node.parentNode, node) + 1) + "]";
    node = node.parentNode
}
return '/' + result.reverse().join("/");