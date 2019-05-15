app.controller("loginController", function ($scope, loginService) {

    $scope.showLoginName = function () {
        loginService.LoginName().success(function (data) {
            $scope.loginName = data.loginUsername;
        })
    }
})