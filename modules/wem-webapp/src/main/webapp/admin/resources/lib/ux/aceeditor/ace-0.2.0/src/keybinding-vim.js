define("ace/keyboard/keybinding/vim", ["require", "exports", "module", "ace/keyboard/state_handler"], function (a, b, c) {
    "use strict";
    var d = a("../state_handler").StateHandler, e = a("../state_handler").matchCharacterOnly, f = function (a, b, c) {
        return{regex: ["([0-9]*)", a], exec: b, params: [
            {name: "times", match: 1, type: "number", defaultValue: 1}
        ], then: c}
    }, g = {start: [
        {key: "i", then: "insertMode"},
        {key: "d", then: "deleteMode"},
        {key: "a", exec: "gotoright", then: "insertMode"},
        {key: "shift-i", exec: "gotolinestart", then: "insertMode"},
        {key: "shift-a", exec: "gotolineend", then: "insertMode"},
        {key: "shift-c", exec: "removetolineend", then: "insertMode"},
        {key: "shift-r", exec: "overwrite", then: "replaceMode"},
        f("(k|up)", "golineup"),
        f("(j|down)", "golinedown"),
        f("(l|right)", "gotoright"),
        f("(h|left)", "gotoleft"),
        {key: "shift-g", exec: "gotoend"},
        f("b", "gotowordleft"),
        f("e", "gotowordright"),
        f("x", "del"),
        f("shift-x", "backspace"),
        f("shift-d", "removetolineend"),
        f("u", "undo"),
        {comment: "Catch some keyboard input to stop it here", match: e}
    ], insertMode: [
        {key: "esc", then: "start"}
    ], replaceMode: [
        {key: "esc", exec: "overwrite", then: "start"}
    ], deleteMode: [
        {key: "d", exec: "removeline", then: "start"}
    ]};
    b.Vim = new d(g)
})