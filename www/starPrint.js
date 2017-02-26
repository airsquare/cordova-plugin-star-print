/*global cordova, module*/

module.exports = {
    printReceipt: function (ip_address, base64_image_str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "StarPrint", "printReceipt", [ip_address, base64_image_str]);
    },
    findPrinters: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "StarPrint", "findPrinters", []);
    }
};
