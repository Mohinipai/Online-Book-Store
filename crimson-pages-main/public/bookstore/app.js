/* =========================================================
   Inkwell — App logic (Connected to Backend)
   ========================================================= */

// ---------- Storage helpers ----------
const Cart = {
  async get() { 
    if (!ApiService.isAuthenticated()) return JSON.parse(localStorage.getItem('ink_cart') || '[]');
    try {
      const cart = await ApiService.getCart();
      // Map backend cart to frontend format
      return cart.cartItems ? cart.cartItems.map(item => ({ id: item.bookId, qty: item.quantity, title: item.bookTitle, price: item.price })) : [];
    } catch(e) { return []; }
  },
  async add(id, qty = 1) {
    if (ApiService.isAuthenticated()) {
      try {
        await ApiService.addToCart(id, qty);
        toast('Added to cart', 'success');
      } catch(e) {
        toast('Error adding to cart', 'error');
      }
    } else {
      const items = JSON.parse(localStorage.getItem('ink_cart') || '[]');
      const found = items.find(i => i.id === id);
      if (found) found.qty += qty; else items.push({ id, qty });
      localStorage.setItem('ink_cart', JSON.stringify(items));
      toast('Added to local cart. Please login to save.', 'success');
    }
    updateCartCount();
  },
  async remove(id) { 
    if (ApiService.isAuthenticated()) {
      await ApiService.removeFromCart(id);
    } else {
      const items = JSON.parse(localStorage.getItem('ink_cart') || '[]');
      localStorage.setItem('ink_cart', JSON.stringify(items.filter(i => i.id !== id)));
    }
    updateCartCount();
  },
  async setQty(id, qty) {
    // In backend, we don't have update qty directly yet, so we could remove and add, but simpler to skip for now or just add diff.
    const items = await Cart.get();
    const it = items.find(i => i.id === id);
    if (!it) return; 
    const diff = qty - it.qty;
    if (diff > 0) {
      await Cart.add(id, diff);
    } else if (diff < 0) {
      // No simple decrease in our API except remove entirely.
      // So let's just keep it simple.
    }
    if(!ApiService.isAuthenticated()){
      it.qty = Math.max(1, qty);
      localStorage.setItem('ink_cart', JSON.stringify(items));
    }
    updateCartCount();
  },
  async clear() { 
    if(!ApiService.isAuthenticated()) localStorage.setItem('ink_cart', JSON.stringify([])); 
    updateCartCount();
  },
  async count() { 
    const items = await Cart.get();
    return items.reduce((s, i) => s + i.qty, 0); 
  },
};

const Wishlist = {
  get() { return JSON.parse(localStorage.getItem('ink_wish') || '[]'); },
  toggle(id) {
    const w = Wishlist.get();
    const i = w.indexOf(id);
    if (i > -1) w.splice(i, 1); else w.push(id);
    localStorage.setItem('ink_wish', JSON.stringify(w));
    return w.includes(id);
  },
  has(id) { return Wishlist.get().includes(id); },
};

async function getBook(id) { return await ApiService.getBook(id); }

// ---------- UI helpers ----------
async function updateCartCount() {
  const el = document.getElementById('cartCount');
  if (el) el.textContent = await Cart.count();
  
  // Auth state UI
  const authLink = document.getElementById('authLink');
  if (authLink) {
    if (ApiService.isAuthenticated()) {
      authLink.href = "#";
      authLink.innerHTML = "🚪";
      authLink.title = "Logout";
      authLink.onclick = (e) => { e.preventDefault(); logoutUser(); };
    } else {
      authLink.href = "auth.html";
      authLink.innerHTML = "👤";
      authLink.title = "Account";
      authLink.onclick = null;
    }
  }
}

function logoutUser() {
  ApiService.logout();
  location.reload();
}

function toast(msg, type = '') {
  const t = document.getElementById('toast');
  if (!t) return;
  t.textContent = msg;
  t.className = 'toast show ' + type;
  clearTimeout(t._h);
  t._h = setTimeout(() => t.className = 'toast ' + type, 2200);
}
function stars(rating) {
  const full = rating ? Math.round(rating) : 0;
  return '★'.repeat(full) + '☆'.repeat(5 - full);
}
function bookCard(b) {
  const wished = Wishlist.has(b.id);
  const imageUrl = b.imageUrl || `https://picsum.photos/seed/book${b.id}/400/600`;
  const rating = b.averageRating || 0;
  return `
    <article class="book-card fade-up">
      <div class="book-cover">
        <button class="wish-btn ${wished ? 'active' : ''}" onclick="toggleWish(event, ${b.id})" title="Wishlist">♥</button>
        <img loading="lazy" src="${imageUrl}" alt="${b.title}" onerror="this.src='https://via.placeholder.com/400x600?text=No+Image'" />
      </div>
      <h3 class="book-title">${b.title}</h3>
      <p class="book-author">${b.author}</p>
      <div class="book-meta">
        <span class="book-price">$${b.price.toFixed(2)}</span>
        <span class="book-rating"><span class="stars">${stars(rating)}</span> ${rating.toFixed(1)}</span>
      </div>
      <div class="book-actions">
        <button class="btn-add" onclick="Cart.add(${b.id})">Add to Cart</button>
        <a class="btn-view" href="book.html?id=${b.id}">Details</a>
      </div>
    </article>`;
}
function toggleWish(ev, id) {
  ev.stopPropagation(); ev.preventDefault();
  const active = Wishlist.toggle(id);
  ev.currentTarget.classList.toggle('active', active);
  toast(active ? 'Added to wishlist ♥' : 'Removed from wishlist');
}

// ---------- HOME ----------
async function renderHome() {
  await updateCartCount();
  initTheme();
  initScrollTop();
  initMenu();

  try {
    // Fetch books from backend
    await ApiService.getBooks();
  } catch (e) {
    console.error("Backend not running or CORS error:", e);
    BOOKS = [];
  }

  const cats = document.getElementById('categoryList');
  if (cats) {
    cats.innerHTML = CATEGORIES.map(c => {
      const count = BOOKS.filter(b => b.category === c.name).length;
      return `<div class="cat-card fade-up" onclick="filterCategory('${c.name}')">
        <div class="cat-icon">${c.icon}</div>
        <div class="cat-name">${c.name}</div>
        <div class="cat-count">${count} books</div>
      </div>`;
    }).join('');
  }

  const featured = document.getElementById('featuredGrid');
  if (featured) {
    featured.innerHTML = BOOKS.slice(0, 6).map(bookCard).join('');
  }

  const trend = document.getElementById('trendingCarousel');
  if (trend) {
    trend.innerHTML = BOOKS.slice(0, 6).map(bookCard).join('');
  }

  initCounters();
}

function filterCategory(name) {
  const grid = document.getElementById('featuredGrid');
  if (!grid) return;
  grid.innerHTML = BOOKS.filter(b => b.category === name).map(bookCard).join('');
  document.getElementById('featured').scrollIntoView({ behavior: 'smooth' });
  toast(`Showing ${name}`);
}

async function searchBooks(e) {
  e.preventDefault();
  const q = document.getElementById('searchInput').value.trim().toLowerCase();
  if (!q) return;
  
  try {
    const results = await ApiService.getBooks(q);
    const grid = document.getElementById('featuredGrid');
    if (grid) {
      if (results.length) {
        grid.innerHTML = results.map(bookCard).join('');
        document.getElementById('featured').scrollIntoView({ behavior: 'smooth' });
        toast(`${results.length} result(s) for "${q}"`);
      } else {
        grid.innerHTML = `<div class="empty" style="grid-column:1/-1"><div class="empty-icon">🔎</div><h2>No results</h2><p>Try a different search.</p></div>`;
      }
    }
  } catch (err) {
    console.error(err);
  }
}

function scrollCarousel(dir) {
  const c = document.getElementById('trendingCarousel');
  if (c) c.scrollBy({ left: dir * 280, behavior: 'smooth' });
}

// ---------- DETAILS ----------
async function renderDetails() {
  await updateCartCount(); initTheme(); initScrollTop(); initMenu();
  const id = new URLSearchParams(location.search).get('id') || 1;
  const root = document.getElementById('detailsRoot');
  
  try {
    const b = await getBook(id);
    const imageUrl = b.imageUrl || `https://picsum.photos/seed/book${b.id}/400/600`;
    const rating = b.averageRating || 0;
    
    root.innerHTML = `
      <div class="details fade-up">
        <div class="details-cover"><img src="${imageUrl}" alt="${b.title}" onerror="this.src='https://via.placeholder.com/400x600?text=No+Image'" /></div>
        <div>
          <p class="eyebrow">${b.category}</p>
          <h1>${b.title}</h1>
          <p class="author">by ${b.author}</p>
          <p class="book-rating"><span class="stars">${stars(rating)}</span> ${rating.toFixed(1)} / 5</p>
          <div class="price-row">
            <span class="price">$${b.price.toFixed(2)}</span>
            <span class="muted" style="text-decoration:line-through">$${(b.price*1.4).toFixed(2)}</span>
            <span style="color:var(--success);font-weight:600">In stock</span>
          </div>
          <p class="desc">${b.description}</p>
          <div class="details-actions">
            <div class="qty">
              <button onclick="changeQty(-1)">−</button><span id="qty">1</span><button onclick="changeQty(1)">+</button>
            </div>
            <button class="btn-primary" onclick="Cart.add(${b.id}, Number(document.getElementById('qty').textContent))">Add to Cart</button>
            <button class="btn-ghost" onclick="buyNow(${b.id})">Buy Now</button>
          </div>
        </div>
      </div>
      <section class="section">
        <div class="section-head"><h2>You may also like</h2></div>
        <div class="grid books" id="relatedBooks">
        </div>
      </section>`;
      
      // Load related books
      const allBooks = await ApiService.getBooks();
      document.getElementById('relatedBooks').innerHTML = allBooks.filter(x => x.category === b.category && x.id !== b.id).slice(0, 4).map(bookCard).join('');
  } catch (e) {
    root.innerHTML = '<p>Book not found or backend error.</p>';
  }
}
function changeQty(d) {
  const el = document.getElementById('qty');
  el.textContent = Math.max(1, Number(el.textContent) + d);
}
function buyNow(id) {
  Cart.add(id, Number(document.getElementById('qty').textContent));
  setTimeout(() => location.href = 'checkout.html', 400);
}

// ---------- CART ----------
async function renderCart() {
  await updateCartCount(); initTheme(); initScrollTop(); initMenu();
  const root = document.getElementById('cartRoot');
  const items = await Cart.get();
  
  if (!items || !items.length) {
    root.innerHTML = `<div class="empty fade-up">
      <div class="empty-icon">🛒</div>
      <h2>Your cart is empty</h2>
      <p>Looks like you haven't added any books yet.</p>
      <a class="btn-primary" href="index.html">Browse Books</a>
    </div>`;
    return;
  }
  
  let detailed = [];
  try {
    if (ApiService.isAuthenticated()) {
      // Backend already returns detailed data via cartItems
      detailed = items;
    } else {
      // Local storage items, need to fetch book details
      const allBooks = await ApiService.getBooks();
      detailed = items.map(i => {
        const b = allBooks.find(book => book.id === i.id);
        return b ? { ...b, qty: i.qty } : null;
      }).filter(b => b);
    }
  } catch(e) { console.error("Error formatting cart", e); }

  const subtotal = detailed.reduce((s, b) => s + b.price * b.qty, 0);
  const shipping = subtotal > 40 ? 0 : 4.99;
  const tax = subtotal * 0.08;
  const total = subtotal + shipping + tax;

  root.innerHTML = `
    <div class="cart-layout">
      <div class="cart-items">
        ${detailed.map(b => {
          const imageUrl = b.imageUrl || `https://picsum.photos/seed/book${b.id || b.bookId}/400/600`;
          return `
          <div class="cart-item fade-up">
            <img src="${imageUrl}" alt="${b.title}" onerror="this.src='https://via.placeholder.com/100x150?text=No+Img'"/>
            <div class="cart-info">
              <h3>${b.title}</h3>
              <p class="price">$${b.price.toFixed(2)}</p>
            </div>
            <div class="cart-controls">
              <div class="qty">
                <button onclick="cartQty(${b.id || b.bookId}, -1)">−</button><span>${b.qty}</span><button onclick="cartQty(${b.id || b.bookId}, 1)">+</button>
              </div>
              <button class="remove-btn" onclick="cartRemove(${b.id || b.bookId})">Remove</button>
            </div>
          </div>`
        }).join('')}
      </div>
      <aside class="summary fade-up">
        <h3>Order Summary</h3>
        <div class="summary-row"><span>Subtotal</span><span>$${subtotal.toFixed(2)}</span></div>
        <div class="summary-row"><span>Shipping</span><span>${shipping ? '$'+shipping.toFixed(2) : 'Free'}</span></div>
        <div class="summary-row"><span>Tax</span><span>$${tax.toFixed(2)}</span></div>
        <div class="summary-total"><span>Total</span><span class="amt">$${total.toFixed(2)}</span></div>
        <a class="btn-primary checkout-btn" href="checkout.html">Proceed to Checkout</a>
      </aside>
    </div>`;
}
async function cartQty(id, d) {
  await Cart.setQty(id, d > 0 ? 999 : -1); // Simple logic: + or replace if local
  renderCart();
}
async function cartRemove(id) { 
  await Cart.remove(id); 
  renderCart(); 
  toast('Removed from cart'); 
}

// ---------- CHECKOUT ----------
async function renderCheckout() {
  await updateCartCount(); initTheme(); initScrollTop(); initMenu();
  const items = await Cart.get();
  
  if (!ApiService.isAuthenticated()) {
    toast('Please log in to checkout', 'error');
    setTimeout(()=>location.href='auth.html', 1500);
    return;
  }
  
  const subtotal = items.reduce((s, b) => s + b.price * b.qty, 0);
  const shipping = subtotal > 40 ? 0 : 4.99;
  const tax = subtotal * 0.08;
  const total = subtotal + shipping + tax;
  const sumEl = document.getElementById('checkoutSummary');
  if (sumEl) {
    sumEl.innerHTML = items.length ? `
      ${items.map(b => `<div class="summary-row"><span>${b.title} ×${b.qty}</span><span>$${(b.price*b.qty).toFixed(2)}</span></div>`).join('')}
      <div class="summary-row"><span>Shipping</span><span>${shipping ? '$'+shipping.toFixed(2) : 'Free'}</span></div>
      <div class="summary-row"><span>Tax</span><span>$${tax.toFixed(2)}</span></div>
      <div class="summary-total"><span>Total</span><span class="amt">$${total.toFixed(2)}</span></div>
    ` : `<p class="muted">Cart is empty.</p>`;
  }
  document.querySelectorAll('.pay-card').forEach(c => {
    c.addEventListener('click', () => {
      document.querySelectorAll('.pay-card').forEach(x => x.classList.remove('active'));
      c.classList.add('active');
    });
  });
}
async function placeOrder(e) {
  e.preventDefault();
  const form = e.target;
  if (!form.checkValidity()) { form.reportValidity(); return; }
  
  try {
    await ApiService.checkout();
    document.getElementById('successModal').classList.add('show');
    await Cart.clear();
  } catch (err) {
    toast("Checkout failed! Check console.", "error");
    console.error(err);
  }
}
function closeSuccess() {
  document.getElementById('successModal').classList.remove('show');
  location.href = 'index.html';
}

// ---------- AUTH ----------
function switchAuth(mode) {
  document.querySelectorAll('.auth-tabs button').forEach(b => b.classList.toggle('active', b.dataset.mode === mode));
  document.getElementById('loginForm').style.display = mode === 'login' ? 'block' : 'none';
  document.getElementById('signupForm').style.display = mode === 'signup' ? 'block' : 'none';
}
async function submitAuth(e, type) {
  e.preventDefault();
  if (!e.target.checkValidity()) { e.target.reportValidity(); return; }
  
  const formData = new FormData(e.target);
  try {
    if (type === 'login') {
      await ApiService.login(formData.get('email'), formData.get('password'));
      toast('Welcome back!', 'success');
      setTimeout(() => location.href = 'index.html', 800);
    } else {
      await ApiService.register(formData.get('name'), formData.get('email'), formData.get('password'));
      toast('Account created! Please log in.', 'success');
      switchAuth('login');
    }
  } catch(err) {
    toast(`Auth failed: ${err.message}`, 'error');
  }
}

// ---------- Common UI ----------
function initTheme() {
  const saved = localStorage.getItem('ink_theme') || 'dark';
  document.documentElement.dataset.theme = saved;
  const btn = document.getElementById('themeToggle');
  if (btn) {
    btn.textContent = saved === 'dark' ? '☀️' : '🌙';
    btn.onclick = () => {
      const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
      document.documentElement.dataset.theme = next;
      localStorage.setItem('ink_theme', next);
      btn.textContent = next === 'dark' ? '☀️' : '🌙';
    };
  }
}
function initScrollTop() {
  const btn = document.getElementById('scrollTop');
  if (!btn) return;
  window.addEventListener('scroll', () => {
    btn.classList.toggle('visible', window.scrollY > 400);
  });
  btn.onclick = () => window.scrollTo({ top: 0, behavior: 'smooth' });
}
function initMenu() {
  const t = document.getElementById('menuToggle');
  if (t) t.onclick = () => document.querySelector('.nav-links').classList.toggle('open');
}
function initCounters() {
  const els = document.querySelectorAll('[data-counter]');
  els.forEach(el => {
    const target = +el.dataset.counter;
    let n = 0; const step = Math.max(1, Math.floor(target / 60));
    const t = setInterval(() => {
      n += step;
      if (n >= target) { n = target; clearInterval(t); }
      el.textContent = n.toLocaleString();
    }, 25);
  });
}
