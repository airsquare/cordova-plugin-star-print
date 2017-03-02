/*global cordova, module*/

module.exports = {
    printReceipt: function (ip_address, base64_image_str, paper, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "StarPrint", "printReceipt", [ip_address, base64_image_str, paper]);
    },
    findPrinters: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "StarPrint", "findPrinters", []);
    }
};
