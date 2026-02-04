(() => {
  const func = (root, initTheme, changeTheme) => {
    const $ = (s) => {
      let dom = root.querySelectorAll(s);
      return dom.length == 1 ? dom[0] : dom;
    };
    let mainButton = $(".main-button");
    let daytimeBackground = $(".daytime-background");
    let cloud = $(".cloud");
    let cloudLight = $(".cloud-light");
    let components = $(".components");
    let moon = $(".moon");
    let stars = $(".stars");
    
    // 初始化状态 (根据传入的 value 判断)
    let isMoved = initTheme === 'dark';
    let isClicked = false;

    // 渲染函数：根据 isMoved 状态设置样式
    const renderState = (dark) => {
      if (!dark) { // 白天状态
        mainButton.style.transform = "translateX(0)";
        mainButton.style.backgroundColor = "rgba(255, 195, 35,1)";
        mainButton.style.boxShadow = "3em 3em 5em rgba(0, 0, 0, 0.5), inset -3em -5em 3em -3em rgba(0, 0, 0, 0.5), inset 4em 5em 2em -2em rgba(255, 230, 80,1)";
        daytimeBackground.forEach(el => el.style.transform = "translateX(0)");
        cloud.style.transform = "translateY(10em)";
        cloudLight.style.transform = "translateY(10em)";
        components.style.backgroundColor = "rgba(70, 133, 192,1)";
        moon.forEach(el => el.style.opacity = "0");
        stars.style.transform = "translateY(-125em)";
        stars.style.opacity = "0";
      } else { // 黑夜状态
        mainButton.style.transform = "translateX(110em)";
        mainButton.style.backgroundColor = "rgba(195, 200,210,1)";
        mainButton.style.boxShadow = "3em 3em 5em rgba(0, 0, 0, 0.5), inset -3em -5em 3em -3em rgba(0, 0, 0, 0.5), inset 4em 5em 2em -2em rgba(255, 255, 210,1)";
        daytimeBackground[0].style.transform = "translateX(110em)";
        daytimeBackground[1].style.transform = "translateX(80em)";
        daytimeBackground[2].style.transform = "translateX(50em)";
        cloud.style.transform = "translateY(80em)";
        cloudLight.style.transform = "translateY(80em)";
        components.style.backgroundColor = "rgba(25,30,50,1)";
        moon.forEach(el => el.style.opacity = "1");
        stars.style.transform = "translateY(-62.5em)";
        stars.style.opacity = "1";
      }
    };

    // 初始化渲染
    renderState(isMoved);

    // 点击事件
    components.onclick = () => {
      if (isClicked) return;
      
      // 切换状态
      isMoved = !isMoved;
      renderState(isMoved);
      
      // 向外部发送 change 事件，传递当前是 dark 还是 light
      changeTheme(isMoved ? 'dark' : 'light');

      isClicked = true;
      setTimeout(() => isClicked = false, 500);
    };

    // 鼠标移入移出特效 (仅微调位置)
    mainButton.addEventListener("mousemove", () => {
      if (isClicked) return;
      if (isMoved) {
         mainButton.style.transform = "translateX(100em)";
         daytimeBackground[0].style.transform = "translateX(100em)";
         daytimeBackground[1].style.transform = "translateX(73em)";
      } else {
         mainButton.style.transform = "translateX(10em)";
         daytimeBackground[0].style.transform = "translateX(10em)";
         daytimeBackground[1].style.transform = "translateX(7em)";
      }
    });
    mainButton.addEventListener("mouseout", () => {
      if (isClicked) return;
      if (isMoved) {
         mainButton.style.transform = "translateX(110em)";
         daytimeBackground[0].style.transform = "translateX(110em)";
      } else {
         mainButton.style.transform = "translateX(0em)";
         daytimeBackground[0].style.transform = "translateX(0em)";
      }
    });
  };

  class ThemeButton extends HTMLElement {
    constructor() { super(); }
    connectedCallback() {
      const initTheme = this.getAttribute("value") || "light";
      const size = +this.getAttribute("size") || 3;
      const shadow = this.attachShadow({ mode: "closed" });
      const container = document.createElement("div");
      container.setAttribute("class", "container");
      // 关键：通过 font-size 控制组件整体缩放，3px 约等于基准
      container.setAttribute("style", `font-size: ${(size / 3).toFixed(2)}px`);
      
      container.innerHTML = `
        <div class="components">
          <div class="main-button">
            <div class="moon"></div><div class="moon"></div><div class="moon"></div>
          </div>
          <div class="daytime-background"></div><div class="daytime-background"></div><div class="daytime-background"></div>
          <div class="cloud">
            <div class="cloud-son"></div><div class="cloud-son"></div><div class="cloud-son"></div>
            <div class="cloud-son"></div><div class="cloud-son"></div><div class="cloud-son"></div>
          </div>
          <div class="cloud-light">
            <div class="cloud-son"></div><div class="cloud-son"></div><div class="cloud-son"></div>
            <div class="cloud-son"></div><div class="cloud-son"></div><div class="cloud-son"></div>
          </div>
          <div class="stars">
            <div class="star big"><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div></div>
            <div class="star big"><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div></div>
            <div class="star medium"><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div></div>
            <div class="star medium"><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div></div>
            <div class="star small"><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div></div>
            <div class="star small"><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div><div class="star-son"></div></div>
          </div>
        </div>`;

      const style = document.createElement("style");
      // 核心 CSS：position: relative + inline-block，确保可以嵌入导航栏
      style.textContent = `
        * { margin: 0; padding: 0; transition: 0.7s; box-sizing: content-box; }
        .container { position: relative; width: 180em; height: 70em; display: inline-block; vertical-align: middle; }
        .components { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(70, 133, 192,1); border-radius: 100em; box-shadow: inset 0 0 5em 3em rgba(0, 0, 0, 0.5); overflow: hidden; cursor: pointer; }
        .main-button { margin: 7.5em 0 0 7.5em; width: 55em; height:55em; background-color: rgba(255, 195, 35,1); border-radius: 50%; box-shadow:3em 3em 5em rgba(0, 0, 0, 0.5), inset -3em -5em 3em -3em rgba(0, 0, 0, 0.5), inset 4em 5em 2em -2em rgba(255, 230, 80,1); transition: 1.0s; }
        .moon { position: absolute; background-color: rgba(150, 160, 180, 1); border-radius: 50%; opacity: 0; }
        .moon:nth-child(1){ top: 7.5em; left: 25em; width: 12.5em; height: 12.5em; }
        .moon:nth-child(2){ top: 20em; left: 7.5em; width: 20em; height: 20em; }
        .moon:nth-child(3){ top: 32.5em; left: 32.5em; width: 12.5em; height: 12.5em; }
        .daytime-background { position: absolute; border-radius: 50%; transition: 1.0s; }
        .daytime-background:nth-child(2){ top: -20em; left: -20em; width: 110em; height:110em; background-color: rgba(255, 255, 255,0.2); z-index: -2; }
        .daytime-background:nth-child(3){ top: -32.5em; left: -17.5em; width: 135em; height:135em; background-color: rgba(255, 255, 255,0.1); z-index: -3; }
        .daytime-background:nth-child(4){ top: -45em; left: -15em; width: 160em; height:160em; background-color: rgba(255, 255, 255,0.05); z-index: -4; }
        .cloud, .cloud-light { transform: translateY(10em); transition: 1.0s; }
        .cloud-son { position: absolute; background-color: #fff; border-radius: 50%; z-index: -1; }
        .cloud-son:nth-child(1){ right: -20em; bottom: 10em; width: 50em; height: 50em; }
        .cloud-son:nth-child(2) { right: -10em; bottom: -25em; width: 60em; height: 60em; }
        .cloud-son:nth-child(3) { right: 20em; bottom: -40em; width: 60em; height: 60em; }
        .cloud-son:nth-child(4) { right: 50em; bottom: -35em; width: 60em; height: 60em; }
        .cloud-son:nth-child(5) { right: 75em; bottom: -60em; width: 75em; height: 75em; }
        .cloud-son:nth-child(6) { right: 110em; bottom: -50em; width: 60em; height: 60em; }
        .cloud { z-index: -2; }
        .cloud-light { position: absolute; right: 0em; bottom: 25em; opacity: 0.5; z-index: -3; }
        .stars { transform: translateY(-125em); z-index: -2; transition: 1.0s; }
        .big { --size: 7.5em; } .medium { --size: 5em; } .small { --size: 3em; }
        .star { position: absolute; width: calc(2*var(--size)); height: calc(2*var(--size)); }
        .star:nth-child(1){ top: 11em; left: 39em; } .star:nth-child(2){ top: 39em; left: 91em; } 
        .star-son { float: left; width: var(--size); height: var(--size); background-image: radial-gradient(circle var(--size) at var(--pos), transparent var(--size), #fff); }
        .star-son:nth-child(1) { --pos: left 0; } .star-son:nth-child(2) { --pos: right 0; } .star-son:nth-child(3) { --pos: 0 bottom; } .star-son:nth-child(4) { --pos: right bottom; }
      `;
      
      const changeTheme = (detail) => this.dispatchEvent(new CustomEvent("change", { detail }));
      func(container, initTheme, changeTheme);
      shadow.appendChild(style);
      shadow.appendChild(container);
    }
  }
  customElements.define("theme-button", ThemeButton);
})();