(function(){

  /* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1. Confirm delete
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
  document.addEventListener('click', function(e){
    const btn = e.target.closest('[data-confirm]');
    if(!btn) return;
    if(!confirm(btn.getAttribute('data-confirm') || 'Confirmer ?')){
      e.preventDefault();
      e.stopPropagation();
    }
  });

  /* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     2. Score max validation
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
  document.addEventListener('input', function(e){
    const el = e.target;
    if(!el.matches('input[data-max-score]')) return;
    const max = parseFloat(el.getAttribute('data-max-score'));
    const v   = parseFloat(el.value);
    el.setCustomValidity(Number.isFinite(max) && Number.isFinite(v) && v > max ? 'Score max : ' + max : '');
  });

  /* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     3. Table pagination + live search
        Initialise automatiquement chaque
        table ayant data-paginate="N"
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
  document.querySelectorAll('table[data-paginate]').forEach(function(table){
    initTable(table);
  });

  function initTable(table){
    var PAGE_SIZE = parseInt(table.getAttribute('data-paginate')) || 10;
    var currentPage = 1;
    var query = '';

    /* --- find companion elements via data-table-id --- */
    var tid     = table.id;
    var searchInput  = document.querySelector('[data-search="'     + tid + '"]');
    var countEl      = document.querySelector('[data-search-count="' + tid + '"]');
    var totalLabel   = countEl ? countEl.getAttribute('data-total') : '';
    var paginationEl = document.querySelector('[data-pagination="' + tid + '"]');
    var perPageSel   = document.querySelector('[data-per-page="'   + tid + '"]');

    /* --- get data rows (exclude header + empty-state rows) --- */
    function dataRows(){
      return Array.from(table.querySelectorAll('tbody tr:not([data-empty])'));
    }

    /* --- filter rows by search query --- */
    function filteredRows(){
      var rows = dataRows();
      if(!query) return rows;
      return rows.filter(function(r){
        return r.textContent.toLowerCase().includes(query);
      });
    }

    /* --- render the current page --- */
    function render(){
      var rows    = filteredRows();
      var total   = rows.length;
      var pages   = Math.max(1, Math.ceil(total / PAGE_SIZE));
      if(currentPage > pages) currentPage = pages;

      /* show/hide rows */
      dataRows().forEach(function(r){ r.style.display = 'none'; });
      var start = (currentPage - 1) * PAGE_SIZE;
      rows.slice(start, start + PAGE_SIZE).forEach(function(r){ r.style.display = ''; });

      /* empty state row */
      var emptyRow = table.querySelector('tr[data-empty]');
      if(emptyRow) emptyRow.style.display = (total === 0) ? '' : 'none';

      /* counter */
      if(countEl){
        if(query){
          countEl.textContent = total + ' résultat' + (total !== 1 ? 's' : '');
        } else {
          countEl.textContent = totalLabel || (total + ' entrée' + (total !== 1 ? 's' : ''));
        }
      }

      /* pagination bar */
      if(paginationEl) renderPagination(paginationEl, currentPage, pages, total, start, Math.min(start + PAGE_SIZE, total));
    }

    /* --- build pagination HTML --- */
    function renderPagination(el, cur, pages, total, from, to){
      /* info text */
      var info = el.querySelector('.pagination-info');
      if(info){
        if(total === 0){
          info.textContent = 'Aucun résultat';
        } else {
          info.textContent = 'Affichage ' + (from+1) + '–' + to + ' sur ' + total;
        }
      }

      /* controls */
      var ctrl = el.querySelector('.pagination-controls');
      if(!ctrl) return;
      ctrl.innerHTML = '';

      /* Prev button */
      ctrl.appendChild(makeBtn('←', cur <= 1, false, function(){ currentPage--; render(); }));

      /* Page numbers with ellipsis */
      var nums = pageNumbers(cur, pages);
      nums.forEach(function(n){
        if(n === '…'){
          var span = document.createElement('button');
          span.className = 'page-btn ellipsis';
          span.textContent = '…';
          ctrl.appendChild(span);
        } else {
          ctrl.appendChild(makeBtn(n, false, n === cur, function(p){ return function(){ currentPage = p; render(); }; }(n)));
        }
      });

      /* Next button */
      ctrl.appendChild(makeBtn('→', cur >= pages, false, function(){ currentPage++; render(); }));
    }

    function makeBtn(label, disabled, active, onClick){
      var b = document.createElement('button');
      b.type = 'button';
      b.className = 'page-btn' + (active ? ' active' : '');
      b.textContent = label;
      b.disabled = disabled;
      b.addEventListener('click', onClick);
      return b;
    }

    /* smart page list: always show first, last, current ±1, with … gaps */
    function pageNumbers(cur, total){
      if(total <= 7) return range(1, total);
      var set = new Set([1, total, cur, cur-1, cur+1].filter(function(n){ return n >= 1 && n <= total; }));
      var sorted = Array.from(set).sort(function(a,b){ return a-b; });
      var result = [];
      for(var i = 0; i < sorted.length; i++){
        if(i > 0 && sorted[i] - sorted[i-1] > 1) result.push('…');
        result.push(sorted[i]);
      }
      return result;
    }

    function range(a, b){ var r = []; for(var i=a;i<=b;i++) r.push(i); return r; }

    /* --- search listener --- */
    if(searchInput){
      searchInput.addEventListener('input', function(){
        query = searchInput.value.trim().toLowerCase();
        currentPage = 1;
        render();
      });
    }

    /* --- per-page selector --- */
    if(perPageSel){
      perPageSel.addEventListener('change', function(){
        PAGE_SIZE = parseInt(perPageSel.value) || 10;
        currentPage = 1;
        render();
      });
    }

    /* --- initial render --- */
    render();
  }

})();

  /* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     4. Sidebar toggle (mobile)
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
  var sidebar  = document.getElementById('sidebar');
  var overlay  = document.getElementById('sidebarOverlay');
  var hamburger= document.getElementById('hamburger');
  var closeBtn = document.getElementById('sidebarClose');

  function openSidebar(){
    if(!sidebar) return;
    sidebar.classList.add('open');
    overlay && overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
  }

  function closeSidebar(){
    if(!sidebar) return;
    sidebar.classList.remove('open');
    overlay && overlay.classList.remove('active');
    document.body.style.overflow = '';
  }

  hamburger && hamburger.addEventListener('click', openSidebar);
  closeBtn  && closeBtn.addEventListener('click', closeSidebar);
  overlay   && overlay.addEventListener('click', closeSidebar);

  // Close on Escape
  document.addEventListener('keydown', function(e){
    if(e.key === 'Escape') closeSidebar();
  });

  // Close sidebar when window resized to desktop
  window.addEventListener('resize', function(){
    if(window.innerWidth > 768) closeSidebar();
  });
