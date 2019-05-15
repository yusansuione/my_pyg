app.service("cartService", function ($http) {
    /**
     * 查询购物车列表
     * @returns {*}
     */
    this.findCartList = function () {
        return $http.get('cart/findCartList.do')
    }
    /**
     * 添加购物车
     * @param num
     * @returns {*}
     */
    this.addGoodsToCartList = function (itemId, num) {
        return $http.post('cart/saveCartList.do?itemId=' + itemId + '&num=' + num)
    }

    /**
     * 计算总价钱
     * @param cartList
     */
    this.sum = function (cartList) {
        var totalValue = {totalNum: 0, totalMoney: 0}
        for (var i = 0; i < cartList.length; i++) {
            var cart = cartList[i];
            for (var j = 0; j < cart.orderItemList.length; j++) {
                var orderItem = cart.orderItemList[j]
                totalValue.totalNum += orderItem.num;
                totalValue.totalMoney += orderItem.totalFee;
            }
        }
        return totalValue;
    }

    //获取地址列表
    this.findAddressList = function () {
        return $http.get('address/findListByLoginUser.do');
    }

    //增加
    this.add = function (order) {
        return $http.post('../order/add.do', order);
    }

})