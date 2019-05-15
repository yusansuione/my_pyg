app.controller("loginController", function ($scope, loginService) {

    $scope.showLoginName = function () {
        loginService.loginService().success(function (responces) {
            $scope.loginName = responces.loginName;
        })
    }

})