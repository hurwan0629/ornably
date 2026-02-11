<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>상품 목록</title>
  <style>
    .wrap { max-width: 900px; margin: 20px auto; font-family: Arial; }
    .row { display:flex; gap:10px; margin-bottom: 12px; }
    .grid { display:grid; grid-template-columns: repeat(4, 1fr); gap:12px; }
    .card { border:1px solid #ddd; padding:10px; border-radius:8px; }
    .name { font-weight:700; margin:6px 0; }
    .muted { color:#666; font-size:12px; }
    .pager button { margin-right:6px; }
  </style>
</head>
<body>
<div class="wrap">
  <h2>상품 목록 (API 연동 연습)</h2>

  <div class="row">
    <input id="search" placeholder="검색어" style="flex:1;" />
    <select id="cartegory">
      <option value="all">all</option>
      <option value="tree">tree</option>
      <option value="light">light</option>
      <option value="ball">ball</option>
      <option value="figure">figure</option>
      <option value="wreaths">wreaths</option>
      <option value="etc">etc</option>
    </select>

    <select id="sort">
      <option value="default">default</option>
      <option value="popular">popular</option>
      <option value="new">new</option>
      <option value="new-reverse">new-reverse</option>
      <option value="discount">discount</option>
    </select>

    <select id="dataCount">
      <option value="4">4</option>
      <option value="8" selected>8</option>
      <option value="12">12</option>
    </select>

    <button onclick="loadItems(1)">검색</button>
  </div>

  <div id="status" class="muted"></div>
  <div id="grid" class="grid"></div>

  <div class="pager" style="margin-top:14px;">
    <button onclick="prevPage()">이전</button>
    <span id="pageInfo" class="muted"></span>
    <button onclick="nextPage()">다음</button>
  </div>
</div>

<script>
  let currentPage = 1;
  let maxPages = 1;

  function buildQuery(page) {
    const search = document.getElementById("search").value;
    const cartegory = document.getElementById("cartegory").value;
    const sort = document.getElementById("sort").value;
    const dataCount = document.getElementById("dataCount").value;

    const params = new URLSearchParams();
    if (search) params.append("search", search);
    params.append("cartegory", cartegory);
    params.append("sort", sort);
    params.append("page", page);
    params.append("dataCount", dataCount);
    return params.toString();
  }

  async function loadItems(page) {
    currentPage = page;
    document.getElementById("status").innerText = "로딩중...";

    try {
      const qs = buildQuery(page);
      const res = await fetch("/api/all/item?" + qs);

      if (!res.ok) {
        document.getElementById("status").innerText = "요청 실패: HTTP " + res.status;
        return;
      }

      const data = await res.json();
      maxPages = data.maxPages || 1;

      render(data.itemDatas || []);
      document.getElementById("status").innerText = "완료";
      document.getElementById("pageInfo").innerText = `page ${currentPage} / ${maxPages}`;
    } catch (e) {
      document.getElementById("status").innerText = "네트워크 오류: " + e;
    }
  }

  function render(items) {
    const grid = document.getElementById("grid");
    grid.innerHTML = "";

    if (items.length === 0) {
      grid.innerHTML = "<div class='muted'>상품이 없습니다.</div>";
      return;
    }

    items.forEach(it => {
      const div = document.createElement("div");
      div.className = "card";
      div.innerHTML = `
        <div class="muted">#${it.itemPk} / ${it.itemCartegory}</div>
        <div class="name">${it.itemName}</div>
        <div>가격: ${it.itemPrice.toLocaleString()}원</div>
        <div>할인율: ${it.itemDiscountRate}% → ${it.itemDiscountPrice.toLocaleString()}원</div>
        <div class="muted">평점: ${it.itemAvgStar}</div>
        <div class="muted">이미지: ${it.itemImageUrl}</div>
      `;
      grid.appendChild(div);
    });
  }

  function prevPage() {
    if (currentPage > 1) loadItems(currentPage - 1);
  }

  function nextPage() {
    if (currentPage < maxPages) loadItems(currentPage + 1);
  }

  // 최초 1페이지 로드
  loadItems(1);
</script>
</body>
</html>