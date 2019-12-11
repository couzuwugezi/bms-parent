(function (w) {
    w.BD = w.BD || {};
    // 缓存所有window对象
    BD.__window = {};
    // 默认配置信息
    BD.curWwwPath = window.document.location.href;         //获取主机地址之后的目录，如： myproj/view/my.jsp
    BD.pathName = window.document.location.pathname;
    BD.pos = BD.curWwwPath.indexOf(BD.pathName);      //获取主机地址，如： http://localhost:8083
    BD.localhostPath = BD.curWwwPath.substring(0, BD.pos);       //获取带"/"的项目名，如：/myproj
    BD.projectName = BD.pathName.substring(0, BD.pathName.substr(1).indexOf('/') + 1);      //得到了 http://localhost:8083/myproj
    BD.realPath = BD.localhostPath + BD.projectName;
})(window);

Vue.prototype.$getRequestParmValue = function (url, paras) {
    let paraString = url.substring(url.indexOf("?") + 1, url.length).split("&");
    let params = {};
    for (let param of paraString) {
        let arr = param.split("=");
        params[arr[0]] = arr[1];
    }
    if (paras) {
        return params[paras]
    }
    return undefined
};

function add0(m) {
    return m < 10 ? '0' + m : m
}

function timestampToTime(shijianchuo) {
    if (shijianchuo == null || shijianchuo === '' || shijianchuo === undefined) {
        return '';
    }
    let time = new Date(shijianchuo);
    let y = time.getFullYear();
    let m = time.getMonth() + 1;
    let d = time.getDate();
    let h = time.getHours();
    let mm = time.getMinutes();
    let s = time.getSeconds();
    return y + '-' + add0(m) + '-' + add0(d) + ' ' + add0(h) + ':' + add0(mm) + ':' + add0(s);
}

async function loadAsyncData(uri) {
    return await axios.get(BD.localhostPath + uri).then(function (response) {
        return response.data;
    }).catch(function (error) {
        console.log(error);
    });
}

/**
 * 计算表格页面table的高度
 * @param vm
 */
function calHeight(vm) {
    let queryBlockHright = document.getElementById("queryBlock").scrollHeight;
    let paginationHright = document.getElementById("pagination").scrollHeight;
    let headerHeight = queryBlockHright + paginationHright + 16;
    vm.height = document.documentElement.clientHeight - headerHeight;
    window.onresize = function temp() {
        vm.height = document.documentElement.clientHeight - headerHeight;
    };
}

let checkPhone = (rule, value, callback) => {
    if (value && !(/^1[3456789]\d{9}$/.test(value))) {
        callback(new Error('请输入正确手机号'));
    } else {
        callback();
    }
};