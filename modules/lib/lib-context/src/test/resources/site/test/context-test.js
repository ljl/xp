var assert = require('/lib/xp/assert');
var context = require('/lib/xp/context');

exports.testNoChange = function () {
    var result = context.run({}, function () {
        return context.get();
    });

    assert.assertJsonEquals({
        "branch": "draft",
        "repository": "cms-repo",
        "authInfo": {
            "principals": [
                "user:system:anonymous",
                "role:system.everyone"
            ]
        }
    }, result);
};

exports.testChange = function () {
    var result = context.run({
        branch: 'mybranch',
        user: {
            login: 'su',
            userStore: 'system'
        },
        principals: ["role:system.myrole"]
    }, function () {
        return context.get();
    });

    assert.assertJsonEquals({
        "branch": "mybranch",
        "repository": "cms-repo",
        "authInfo": {
            "user": {
                "type": "user",
                "key": "user:system:su",
                "displayName": "Super User",
                "disabled": false,
                "login": "su",
                "userStore": "system"
            },
            "principals": [
                "role:system.admin",
                "role:system.everyone",
                "user:system:su",
                "role:system.myrole"
            ]
        }
    }, result);
};

function runExample(name) {
    testInstance.runScript('/site/lib/xp/examples/context/' + name + '.js');
}

exports.testExamples = function () {
    runExample('get');
    runExample('run');
};
