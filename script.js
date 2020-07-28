let node = document.evaluate("//*[contains(text(), \'Показать телефон\')]",
    document,
    null,
    XPathResult.FIRST_ORDERED_NODE_TYPE,
    null).singleNodeValue;

let result = [];
function find_son(father, son) {
    const children = father.children();
    for (let i = 0; i < children.length; i++) {
        if (children[i] === father) {
            return i;
        }
    }
}

while (true) {
    result.push(node.name);
    if (node.name === "html") {
        break;
    }
    result[result.length - 1] = result[result.length - 1] + "[" + find_son(node.parentNode, node) + "]";
}
return result;
