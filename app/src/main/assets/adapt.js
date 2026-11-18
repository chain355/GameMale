/* Gamemale App — 页面适配脚本（注入到站点每个页面） */
(function () {
  if (window.__gmAdapt) return;
  window.__gmAdapt = 1;

  /* ---------- 1. 状态应用：深色 / 专注 ---------- */
  window.__gmApply = function (opts) {
    opts = opts || {};
    var el = document.documentElement;
    if (typeof opts.dark === "boolean") {
      if (opts.dark) el.classList.add("gm-dark");
      else el.classList.remove("gm-dark");
    }
    if (typeof opts.focus === "boolean") {
      if (opts.focus) el.classList.add("gm-focus");
      else el.classList.remove("gm-focus");
    }
  };

  /* ---------- 2. 图片懒加载修复 ---------- */
  function fixImages() {
    var imgs = document.querySelectorAll("img[file], img[zoomfile]");
    for (var i = 0; i < imgs.length; i++) {
      var img = imgs[i];
      var real = img.getAttribute("file") || img.getAttribute("zoomfile");
      var src = img.getAttribute("src") || "";
      if (!real) continue;
      if (!src || /none\.gif|lazy|loading|blank\.gif/i.test(src)) {
        img.setAttribute("src", real);
      }
      img.removeAttribute("height");
      img.removeAttribute("width");
    }
    // 头像等固定尺寸小图保持原样，正文大图放开高度限制
    var body = document.querySelectorAll(".message img, .t_f img");
    for (var j = 0; j < body.length; j++) {
      body[j].style.maxWidth = "100%";
      body[j].style.height = "auto";
    }
  }

  /* ---------- 3. 正文图片点按全屏查看 ---------- */
  var box = null;
  function ensureBox() {
    if (box) return box;
    box = document.createElement("div");
    box.id = "gm-lightbox";
    var img = document.createElement("img");
    var hint = document.createElement("div");
    hint.className = "hint";
    hint.textContent = "点按任意位置关闭";
    box.appendChild(img);
    box.appendChild(hint);
    box.addEventListener("click", function () {
      box.classList.remove("on");
      box.querySelector("img").setAttribute("src", "");
    });
    (document.body || document.documentElement).appendChild(box);
    return box;
  }

  document.addEventListener(
    "click",
    function (e) {
      var t = e.target;
      if (!t || t.tagName !== "IMG") return;
      if (!t.closest || !t.closest(".message, .t_f, #postlist")) return;
      var w = t.naturalWidth || 0;
      if (w && w < 120) return; // 表情小图不放大
      var big = t.getAttribute("zoomfile") || t.getAttribute("file") || t.src;
      if (!big) return;
      e.preventDefault();
      e.stopPropagation();
      var b = ensureBox();
      b.querySelector("img").setAttribute("src", big);
      b.classList.add("on");
    },
    true
  );

  /* ---------- 4. 楼层跳转 / 返回顶部等小按钮放大 ---------- */
  function tidySmallStuff() {
    // 帖子列表里被压成 block 后，去掉多余的空行
    var empties = document.querySelectorAll("#threadlist .icn, #threadlist .num");
    for (var i = 0; i < empties.length; i++) {
      if (!empties[i].textContent.replace(/\s/g, "").length) empties[i].style.display = "none";
    }
  }

  /* ---------- 5. 游客状态：只在真的没登录时提示 ---------- */
  function isLoggedIn() {
    var st = loginState();
    return st === 1;
  }

  /* 1 = 已登录, 0 = 游客, -1 = 判定不了（这种情况不弹横幅，避免误报） */
  function loginState() {
    var ck = "";
    try { ck = document.cookie || ""; } catch (e) {}
    var uid = ck.match(/discuz_uid=(\d+)/);
    if (uid) return uid[1] === "0" ? 0 : 1;

    // 页面上任何指向“退出”的链接 = 已登录（比 #um 更可靠）
    var as = document.getElementsByTagName("a");
    for (var i = 0; i < as.length; i++) {
      var h = as[i].href || "";
      if (/mod=logging/i.test(h) && /action=logout/i.test(h)) return 1;
    }
    var um = document.getElementById("um");
    var html = um ? um.innerHTML : "";
    // 登录后 #um 里一定有退出链接（action=logout），游客页只有 action=login
    if (/action=logout/.test(html)) return true;
    if (/mod=space&(?:amp;)?uid=\d+/.test(html)) return true;
    if (/class="avt/.test(html)) return 1;
    return -1;
  }

  function guestBar() {
    var exists = document.getElementById("gm-guestbar");
    var st = loginState();
    if (st !== 0) {
      // 已登录或判定不了：一律不显示横幅
      if (exists) exists.parentNode.removeChild(exists);
      return;
    }
    if (exists) return;
    var um = document.getElementById("um");
    var html = um ? um.innerHTML : "";
    if (um && !/mod=logging|登录|登錄/.test(html)) return;

    var bar = document.createElement("div");
    bar.id = "gm-guestbar";
    var txt = document.createElement("span");
    txt.className = "gm-guest-text";
    txt.textContent = "你还没有登录，登录后可看完整内容、回复和下载附件";
    var login = document.createElement("a");
    login.className = "gm-guest-btn";
    login.href = "member.php?mod=logging&action=login&from=" +
      encodeURIComponent(location.pathname + location.search);
    login.textContent = "登录";
    var reg = document.createElement("a");
    reg.className = "gm-guest-btn gm-guest-ghost";
    reg.href = "member.php?mod=register";
    reg.textContent = "注册";
    bar.appendChild(txt);
    bar.appendChild(login);
    bar.appendChild(reg);
    var anchor = document.body.firstChild;
    document.body.insertBefore(bar, anchor);
  }

  /* ---------- 6. 让快速回复框在滚动时保持在视野内 ---------- */
  function focusReply() {
    var t = document.querySelector("#fastpostmessage");
    if (t && window.__gmWantReply) {
      window.__gmWantReply = false;
      t.focus();
      t.scrollIntoView({ block: "center", behavior: "smooth" });
    }
  }

  /* ---------- 6.5 帖子页右下角浮动回复按钮 ---------- */
  function injectFab() {
    if (!/thread-|mod=viewthread/.test(location.href)) return;
    if (!document.getElementById("postlist")) return;
    if (document.getElementById("gm-fab")) return;
    var a = document.createElement("a");
    a.id = "gm-fab";
    a.className = "gm-fab";
    a.href = "javascript:;";
    a.textContent = "回复";
    a.addEventListener("click", function (e) {
      e.preventDefault();
      var fast = document.getElementById("fastpost");
      var anchor = document.getElementById("postlist");
      var target = fast || anchor;
      if (target && target.scrollIntoView) {
        target.scrollIntoView({ behavior: "smooth", block: "end" });
      } else {
        window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" });
      }
      setTimeout(function () {
        var ta = document.querySelector("#fastpostmessage");
        if (ta) { ta.focus(); }
        var win = document.getElementById("fwin_reply");
        if (win && win.querySelector("textarea")) win.querySelector("textarea").focus();
      }, 700);
    });
    (document.body || document.documentElement).appendChild(a);
  }

  /* ---------- 6.6 帖子作者行瘦身：去掉手机上很少用的动作 ---------- */
  function trimPostActions() {
    var links = document.querySelectorAll("#postlist .authi a");
    var drop = ["倒序浏览", "阅读模式", "关注该作者的回复", "回帖奖励", "只看大图"];
    for (var i = 0; i < links.length; i++) {
      var t = links[i].textContent.replace(/\s/g, "");
      if (drop.indexOf(t) >= 0) links[i].style.display = "none";
    }
  }

  /* ---------- 7. 启动 ---------- */
  function boot() {
    fixImages();
    tidySmallStuff();
    guestBar();
    bindFilterTabs();
    focusReply();
    injectFab();
    trimPostActions();
    reportTabs();
  }


  /* ---------- 8. 版块筛选（全部主题/最新/热门…）只重载帖子列表 ---------- */
  var listLoading = false;

  function paintTabs(activeUrl) {
    var tf = document.querySelector("#threadlist .th .tf");
    if (!tf) return;
    var links = tf.querySelectorAll("a");
    for (var i = 0; i < links.length; i++) links[i].classList.remove("gm-on");

    var matched = false;
    if (activeUrl) {
      for (var j = 0; j < links.length; j++) {
        if (links[j].href === activeUrl) { links[j].classList.add("gm-on"); matched = true; }
      }
    }
    if (!matched) {
      for (var k = 0; k < links.length; k++) {
        if (/全部主题/.test(links[k].textContent)) { links[k].classList.add("gm-on"); break; }
      }
    }
  }

  function loadList(url, fromPop) {
    if (listLoading) return;
    listLoading = true;
    var oldList = document.querySelector("#threadlist");
    if (!oldList) { location.href = url; return; }
    var top = oldList.getBoundingClientRect().top + window.scrollY - 70;

    fetch(url, { credentials: "include" })
      .then(function (r) { return r.text(); })
      .then(function (text) {
        var doc = new DOMParser().parseFromString(text, "text/html");
        var newList = doc.querySelector("#threadlist");
        if (!newList) throw new Error("no threadlist");

        // 只替换列表本体，页头/页脚/面包屑都不动
        oldList.innerHTML = newList.innerHTML;

        // 同步底部页码
        var oldPgs = document.querySelector("#ct .pgs, .pgs");
        var newPgs = doc.querySelector("#ct .pgs, .pgs");
        if (oldPgs && newPgs) oldPgs.innerHTML = newPgs.innerHTML;

        // 同步“本版规则/收藏本版”信息条
        var oldInfo = document.querySelector("#ct .bm_h");
        var newInfo = doc.querySelector("#ct .bm_h");
        if (oldInfo && newInfo) oldInfo.innerHTML = newInfo.innerHTML;

        fixImages();
        tidySmallStuff();
        trimPostActions(); 
        var ntf = oldList.querySelector(".th .tf");
        if (ntf) bindFilterTabs();
        paintTabs(url);
        reportTabs();
        if (!fromPop) {
          try { history.pushState({ gmList: url }, "", url); } catch (e) {}
        }
        window.scrollTo(0, Math.max(0, top));
        if (window.__gmAfterList) window.__gmAfterList();
      })
      .catch(function () { location.href = url; })
      .finally(function () { listLoading = false; });
  }

  function bindFilterTabs() {
    var tf = document.querySelector("#threadlist .th .tf");
    if (!tf || tf.__gmBound) return;
    tf.__gmBound = 1;
    tf.__gmBound = 1;
    tf.addEventListener("click", function (e) {
      var a = e.target.closest ? e.target.closest("a") : null;
      if (!a || !a.href) return;
      if (!/filter=|orderby=|digest=/.test(a.href)) return;
      if (a.href.indexOf("javascript") === 0) return;
      e.preventDefault();
      e.stopPropagation();
      loadList(a.href, false);
    }, true);
    paintTabs(null);
  }

  window.addEventListener("popstate", function (e) {
    if (e.state && e.state.gmList) loadList(e.state.gmList, true);
  });

  /* 把筛选项结构上报给原生，由 COUI ChipTabRow 渲染 */
  function reportTabs() {
    try {
      if (!window.GMApp || !window.GMApp.onTabs) return;
      // 优先取“主题分类”条（全部/公告/活动/讨论…），它就是手机上最该做成 tab 的一排
      var tt = document.querySelector("#thread_types");
      if (!tt) { window.GMApp.onTabs(""); return; }
      var out = [];
      var links = tt.querySelectorAll("a");
      for (var i = 0; i < links.length; i++) {
        var a = links[i];
        var href = a.href || "";
        if (!href || href.indexOf("javascript") === 0) continue;
        var label = (a.textContent || "").replace(/\s+/g, "");
        label = label.replace(/\d+$/, ""); // 去掉末尾数量（如“活动5”）
        if (!label) continue;
        var active = (href === location.href) || (a.parentNode && a.parentNode.className && /\ba\b/.test(a.parentNode.className));
        out.push({ label: label, url: href, active: active ? 1 : 0 });
      }
      // 告诉页面：分类条已经由原生渲染，隐藏网页里那一排
      document.documentElement.classList.add("gm-native-tabs");
      window.GMApp.onTabs(JSON.stringify(out));
    } catch (e) {}
  }

  /* 原生点胶囊 tab 时调用，只换列表 */
  window.__gmLoadList = function (url) {
    if (!url) return;
    loadList(url, false);
  };

  /* ---------- 9. 滚动位置上报，供 App 记忆每个页面停留位置 ---------- */
  var scrollTimer = null;
  window.addEventListener("scroll", function () {
    if (scrollTimer) return;
    scrollTimer = setTimeout(function () {
      scrollTimer = null;
      try {
        if (window.GMApp && window.GMApp.onScroll) {
          window.GMApp.onScroll(Math.round(window.scrollY || 0));
        }
      } catch (e) {}
    }, 120);
  }, { passive: true });

  window.__gmScrollTo = function (y) {
    window.scrollTo(0, typeof y === "number" ? y : 0);
  };
  window.__gmScrollY = function () { return Math.round(window.scrollY || 0); };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot);
  } else {
    boot();
  }
  window.addEventListener("load", function () {
    fixImages();
    guestBar();
    bindFilterTabs();
    injectFab();
    trimPostActions();
    reportTabs();
  });

  // 站点异步加载新楼层后重新处理图片
  var mo = window.MutationObserver
    ? new MutationObserver(function () {
        fixImages();
      })
    : null;
  if (mo) {
    mo.observe(document.documentElement, { childList: true, subtree: true });
  }
})();
