app.controller("payController", function ($scope, $location, payService) {

    /**
     * 创建付款二维码
     */
    $scope.createNative = function () {
        payService.createNative().success(function (responce) {
            $scope.money = responce.total_fee;//价格
            $scope.out_trade_no = responce.out_trade;//订单号
            //二维码
            var qrcode = new QRCode(document.getElementById("qrious"), {
                text: responce.code_url,
                width: 128,
                height: 128,
                colorDark: "#000000",
                colorLight: "#ffffff",
                correctLevel: QRCode.CorrectLevel.H
            });

            $scope.queryPayStatus(responce.out_trade);//查询支付状态
        })
    }

    $scope.queryPayStatus = function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (responce) {
            if (responce.success) {
                window.location.href = "paysuccess.html#?money=" + $scope.money;
            } else {
                if (responce.message == "支付超时") {
                    window.location.href = "payOutTime.html"
                }
                window.location.href = "payfail.html";
            }
        })
    }

    $scope.showMoney = function () {
        var money = $location.search()['money'];
        return money;
    }
})