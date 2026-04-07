let allItems = [];

/* ================= AUTH ================= */

async function handleLogin() {
    const email = document.getElementById('email-input').value.toLowerCase();
    const password = document.getElementById('pass-input').value;

    const res = await fetch('/api/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (res.ok) {
        const user = await res.json();
        localStorage.setItem('userEmail', user.email);
        showDashboard();
    } else {
        alert("Login failed.");
    }
}

async function handleRegister() {
    const email = document.getElementById('email-input').value.toLowerCase();
    const password = document.getElementById('pass-input').value;

    if (!email.endsWith("@georgiasouthern.edu")) {
        alert("GSU email only!");
        return;
    }

    const res = await fetch('/api/users/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (res.ok) {
        alert("Account created! Log in now.");
    }
}

function showDashboard() {
    const email = localStorage.getItem('userEmail');
    if (!email) return;

    document.getElementById('login-overlay').style.display = 'none';
    document.getElementById('main-content').style.display = 'block';
    document.getElementById('toolbar').style.display = 'flex';
    document.getElementById('user-initial').innerText =
        email.charAt(0).toUpperCase();
    document.getElementById('user-email-display').innerText = email;

    refreshFeed();
}

/* ================= MENU ================= */

function toggleMenu(e) {
    e.stopPropagation();
    document.getElementById('profileMenu').classList.toggle('active');
}

window.onclick = () =>
    document.getElementById('profileMenu').classList.remove('active');

function logout() {
    localStorage.clear();
    location.reload();
}

/* ================= POSTING ================= */

async function handlePost(e) {
    e.preventDefault();

    const formData = new FormData();
    formData.append("title", document.getElementById('title').value);
    formData.append("description", document.getElementById('description').value);
    formData.append("category", document.getElementById('category').value);
    formData.append("location", document.getElementById('location').value);
    formData.append("posterEmail", localStorage.getItem('userEmail'));

    const imageFile = document.getElementById('image').files[0];
    if (imageFile) formData.append("image", imageFile);

    const response = await fetch('/api/listings', {
        method: 'POST',
        body: formData
    });

    if (response.ok) {
        document.getElementById('postForm').reset();
        document.getElementById("imagePreviewContainer").style.display = "none";

        const fileName = document.getElementById("fileName");
        if (fileName) fileName.textContent = "No file selected";

        refreshFeed();
        window.location.href = "index.html";
    } else {
        console.error("Failed to post item");
    }
}

/* ================= FEED ================= */

async function refreshFeed() {
    const res = await fetch('/api/listings');
    allItems = await res.json();
    renderFeed();
}

function renderFeed() {
    const feed = document.getElementById('food-feed');
    const cat = document.getElementById('filterCategory').value;
    const loc = document.getElementById('filterLocation').value;
    const currentUser = localStorage.getItem('userEmail');

    const filtered = allItems.filter(i =>
        i.status === 'AVAILABLE' &&
        (cat === "" || i.category === cat) &&
        (loc === "" || i.location === loc)
    );

    feed.innerHTML = filtered.map(item => `
            <div class="food-card">
                ${item.imageBase64
            ? `<img src="data:image/jpeg;base64,${item.imageBase64}">`
            : ''}

                <div class="food-category">${item.category}</div>
                <h4>${item.title}</h4>
                <div class="desc">${item.description}</div>
                <div class="food-location">📍 ${item.location}</div>

                ${item.posterEmail === currentUser
            ? `<button class="claim-btn"
                              style="background:#fee2e2; color:#dc2626;"
                              onclick="deletePost(${item.id})">
                          Delete Post
                       </button>`
            : `<button class="claim-btn active"
                              onclick="claim(${item.id})">
                          Claim
                       </button>`
        }
            </div>
        `).join('');
}

async function claim(id) {
    const email = localStorage.getItem('userEmail');

    const res = await fetch(`/api/listings/${id}/claim`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email: email })
    });

    if (res.ok) {
        refreshFeed();
    } else {
        const errorText = await res.text();
        alert(errorText);
    }
}

async function deletePost(id) {
    const reason = prompt("Reason for deleting:");
    if (reason === null) return;

    const res = await fetch(`/api/listings/${id}/delete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            userEmail: localStorage.getItem('userEmail'),
            reason: reason || "No reason provided"
        })
    });

    if (res.ok) {
        refreshFeed();
    } else {
        alert("Failed to delete post.");
    }
}

/* ================= ACTIVITY ================= */

function openActivity() {
    const currentUser = localStorage.getItem('userEmail');

    const myClaims = allItems.filter(i => i.claimedBy === currentUser);
    const myPosts = allItems.filter(i => i.posterEmail === currentUser);

    document.getElementById('claimsList').innerHTML =
        "<h4>Items I Claimed</h4>" +
        (myClaims.length
            ? myClaims.map(i =>
                `<p>✅ <strong>${i.title}</strong><br>
                     📍 ${i.location}</p>`
            ).join("")
            : "<p style='color:#64748b;'>None</p>");

    document.getElementById('postsList').innerHTML =
        "<h4>My Posts</h4>" +
        (myPosts.length
            ? myPosts.map(i =>
                `<p>
                        📦 <strong>${i.title}</strong><br>
                        Status: <b>${i.status}</b>
                        ${i.claimedBy
                    ? `<br><small style="color:#0f172a;">
                                 Claimed by: ${i.claimedBy}
                               </small>`
                    : ""}
                    </p>`
            ).join("")
            : "<p style='color:#64748b;'>None</p>");

    document.getElementById('activityModal')
        .classList.add('active');
}

function closeActivity() {
    document.getElementById('activityModal')
        .classList.remove('active');
}

/* ================= IMAGE PREVIEW ================= */

function previewImage(event) {
    const file = event.target.files[0];
    if (!file) return;

    document.getElementById("fileName").textContent = file.name;

    const reader = new FileReader();
    reader.onload = function (e) {
        document.getElementById("imagePreview").src = e.target.result;
        document.getElementById("imagePreviewContainer").style.display = "block";
    };
    reader.readAsDataURL(file);
}

/* ================= AUTO INIT ================= */

if (localStorage.getItem('userEmail')) showDashboard();
setInterval(refreshFeed, 5000);