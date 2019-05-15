app.controller("indexController", function ($scope, loginService) {
    $scope.login = function () {
        loginService.loginUsername().success(function (data) {
            $scope.loginName = data;
        })
    }
})