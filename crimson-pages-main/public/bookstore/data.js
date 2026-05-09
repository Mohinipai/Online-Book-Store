// API Configuration
const API_BASE = "http://localhost:8080/api";

const CATEGORIES = [
  { name: "Fiction", icon: "📕" },
  { name: "Sci-Fi", icon: "🚀" },
  { name: "Mystery", icon: "🔍" },
  { name: "Romance", icon: "💕" },
  { name: "History", icon: "🏛️" },
  { name: "Memoir", icon: "✍️" },
  { name: "Self Help", icon: "🌱" },
  { name: "Tech", icon: "💻" },
  { name: "Fantasy", icon: "🐉" },
  { name: "Thriller", icon: "🔪" },
  { name: "Business", icon: "💼" },
  { name: "Biography", icon: "📖" },
];

// Utility function to get auth headers
function getAuthHeaders() {
  const token = localStorage.getItem("token");
  return {
    "Content-Type": "application/json",
    ...(token ? { "Authorization": `Bearer ${token}` } : {})
  };
}

// Global state fallback for frontend display compatibility
let BOOKS = [];

// API Functions
const ApiService = {
  // Authentication
  login: async (email, password) => {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: "Login failed" }));
      throw new Error(errorData.message || "Invalid credentials");
    }
    const data = await res.json();
    localStorage.setItem("token", data.accessToken);
    localStorage.setItem("userName", data.name || "");
    return data;
  },

  register: async (name, email, password) => {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, email, password })
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: "Registration failed" }));
      throw new Error(errorData.message || "Email already exists");
    }
    return res.text();
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userName");
  },

  isAuthenticated: () => {
    return !!localStorage.getItem("token");
  },

  // Books
  getBooks: async (search = "") => {
    const res = await fetch(`${API_BASE}/books?search=${search}&pageSize=200`);
    if (res.status === 401 && ApiService.isAuthenticated()) { 
      ApiService.logout(); location.reload(); return []; 
    }
    if (!res.ok) throw new Error("Failed to fetch books");
    const data = await res.json();
    BOOKS = data.content || data;
    return BOOKS;
  },

  getBook: async (id) => {
    const res = await fetch(`${API_BASE}/books/${id}`);
    if (res.status === 401) { ApiService.logout(); location.reload(); return null; }
    if (!res.ok) throw new Error("Failed to fetch book");
    return res.json();
  },

  // Cart
  getCart: async () => {
    const res = await fetch(`${API_BASE}/cart`, { headers: getAuthHeaders() });
    if (res.status === 401) { ApiService.logout(); location.reload(); return { cartItems: [] }; }
    if (!res.ok) throw new Error("Failed to fetch cart");
    return res.json();
  },

  addToCart: async (bookId, quantity = 1) => {
    const res = await fetch(`${API_BASE}/cart/${bookId}?quantity=${quantity}`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (res.status === 401) { ApiService.logout(); location.reload(); throw new Error("Unauthorized"); }
    if (!res.ok) throw new Error("Failed to add to cart");
    return res.json();
  },

  removeFromCart: async (bookId) => {
    const res = await fetch(`${API_BASE}/cart/${bookId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (res.status === 401) { ApiService.logout(); location.reload(); throw new Error("Unauthorized"); }
    if (!res.ok) throw new Error("Failed to remove from cart");
    return res.text();
  },

  // Checkout
  checkout: async () => {
    const res = await fetch(`${API_BASE}/orders/checkout`, {
      method: 'POST',
      headers: getAuthHeaders()
    });
    if (res.status === 401) { ApiService.logout(); location.reload(); throw new Error("Unauthorized"); }
    if (!res.ok) throw new Error("Checkout failed");
    return res.json();
  }
};
