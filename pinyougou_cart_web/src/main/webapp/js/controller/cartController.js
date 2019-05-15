app.controller("cartController", function ($scope, cartService) {

    /**
     * 查询购物车
     */
    $scope.findCartList = function () {
        cartService.findCartList().success(function (responce) {
            $scope.cartList = responce;
            $scope.totalValue = cartService.sum($scope.cartList);//计算总和
        })
    }

    /**
     * 添加购物车
     * @param itemId
     * @param num
     */
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(function (responces) {
            if (responces.success) {
                $scope.findCartList();//刷新列表
            } else {
                alert(responces.message);
            }
        })
    }

    //查询用户地址
    $scope.findAddressList = function () {
        cartService.findAddressList().success(function (responces) {
            $scope.addressList = responces;
            for (var i = 0; i < responces.length; i++) {
                if (responces[i].isDefault == '1') {
                    $scope.address = responces[i];
                }
            }
        })
    }

    //选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }

    $scope.isSelectAddress = function (address) {
        if ($scope.address == address) {
            return true;
        } else {
            return false;
        }
    }

    $scope.order = {paymentType: 1}
    //定义支付方式
    $scope.selectPayType = function (paymentType) {
        $scope.order.paymentType = paymentType;
    }


    //保存
    $scope.save = function () {
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//电话号码
        $scope.order.receiver = $scope.address.contact//联系人
        var serviceObject = cartService.add($scope.order).success(
            function (response) {
                if (response.success) {
                    if ($scope.order.paymentType == 1) {
                        location.href = "pay.html";
                    } else {
                        location.href = "paysuccess.html";
                    }
                } else {
                    location.href = "payfail.html";
                }
            }
        );
    }

})