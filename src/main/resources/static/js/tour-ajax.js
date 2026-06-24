function updatePriceLabel(value) {
    document.getElementById('current-price-label').innerText = new Intl.NumberFormat('vi-VN').format(value) + 'đ';
}

function triggerFilter() {
    let type = document.getElementById('type-select').value;
    let maxPrice = document.getElementById('price-range').value;
    let sortSelect = document.getElementById('sort-select');
    let sort = sortSelect ? sortSelect.value : null;
    let transportationCheckboxes = document.querySelectorAll('input[name="transportation"]:checked');

    let transportations = Array.from(transportationCheckboxes).map(cb => cb.value);

    let url = new URL(window.location.href);
    url.searchParams.set('page', '1'); // Reset to page 1 on filter

    if (type) {
        url.searchParams.set('type', type);
    } else {
        url.searchParams.delete('type');
    }

    url.searchParams.delete('transportations');
    transportations.forEach(t => url.searchParams.append('transportations', t));

    url.searchParams.set('maxPrice', maxPrice);

    if (sort) {
        url.searchParams.set('sort', sort);
    } else {
        url.searchParams.delete('sort');
    }

    fetch(url, {
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
        .then(response => response.text())
        .then(html => {
            let container = document.getElementById('tourListContainer');
            if (container) {
                container.outerHTML = html;
            }
            window.history.pushState({ path: url.toString() }, '', url.toString());
        })
        .catch(error => console.error('Error fetching filtered tours:', error));
}

document.addEventListener('DOMContentLoaded', function () {
    // Intercept clicks on pagination links inside the container
    document.body.addEventListener('click', function (e) {
        let pageLink = e.target.closest('.custom-pagination .page-link');
        if (pageLink) {
            let parentLi = pageLink.parentElement;
            if (!parentLi.classList.contains('disabled') && !parentLi.classList.contains('active')) {
                e.preventDefault(); // Prevent standard page load
                let url = pageLink.getAttribute('href');

                // Copy existing filter params to the pagination link
                let currentUrl = new URL(window.location.href);
                let fetchUrl = new URL(url, window.location.origin);

                if (currentUrl.searchParams.has('type')) fetchUrl.searchParams.set('type', currentUrl.searchParams.get('type'));
                if (currentUrl.searchParams.has('maxPrice')) fetchUrl.searchParams.set('maxPrice', currentUrl.searchParams.get('maxPrice'));
                if (currentUrl.searchParams.has('sort')) fetchUrl.searchParams.set('sort', currentUrl.searchParams.get('sort'));
                currentUrl.searchParams.getAll('transportations').forEach(t => fetchUrl.searchParams.append('transportations', t));

                // Fetch the new page fragment via AJAX
                fetch(fetchUrl, {
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                    .then(response => response.text())
                    .then(html => {
                        // Replace the entire container with the new HTML fragment
                        let container = document.getElementById('tourListContainer');
                        if (container) {
                            container.outerHTML = html;
                        }

                        // Update browser history URL
                        window.history.pushState({ path: fetchUrl.toString() }, '', fetchUrl.toString());
                    })
                    .catch(error => console.error('Error fetching tours:', error));
            }
        }
    });

    // Handle browser back/forward buttons
    window.addEventListener('popstate', function (e) {
        if (e.state && e.state.path) {
            fetch(e.state.path, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
                .then(response => response.text())
                .then(html => {
                    let container = document.getElementById('tourListContainer');
                    if (container) container.outerHTML = html;
                });
        } else {
            window.location.reload();
        }
    });
});
