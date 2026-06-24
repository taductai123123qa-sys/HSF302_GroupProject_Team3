// Restaurant Slider Script
let currentRestaurantSlide = 0;
let restaurantSliderInterval;

function moveRestaurantSlider(direction) {
    const track = document.getElementById('restaurantSlider');
    if (!track) return;
    const slides = track.querySelectorAll('img');
    const totalSlides = slides.length;

    currentRestaurantSlide += direction;
    if (currentRestaurantSlide < 0) {
        currentRestaurantSlide = totalSlides - 1;
    } else if (currentRestaurantSlide >= totalSlides) {
        currentRestaurantSlide = 0;
    }

    track.style.transform = `translateX(-${currentRestaurantSlide * 100}%)`;

    // Reset interval when manual navigation occurs
    if (direction !== 0) {
        resetRestaurantSliderInterval();
    }
}

function startRestaurantSliderAutoPlay() {
    const track = document.getElementById('restaurantSlider');
    if (!track) return;
    // Change image every 6 seconds
    restaurantSliderInterval = setInterval(() => {
        moveRestaurantSlider(1);
    }, 6000);
}

function resetRestaurantSliderInterval() {
    clearInterval(restaurantSliderInterval);
    startRestaurantSliderAutoPlay();
}

// Start autoplay on load
document.addEventListener('DOMContentLoaded', () => {
    startRestaurantSliderAutoPlay();
});

// Room Slider Script
let currentRoomSlide = 0;
let roomSliderInterval;

function moveRoomSlider(direction) {
    const track = document.getElementById('roomSlider');
    if (!track) return;
    const slides = track.querySelectorAll('img');
    const totalSlides = slides.length;

    currentRoomSlide += direction;
    if (currentRoomSlide < 0) {
        currentRoomSlide = totalSlides - 1;
    } else if (currentRoomSlide >= totalSlides) {
        currentRoomSlide = 0;
    }

    track.style.transform = `translateX(-${currentRoomSlide * 100}%)`;

    if (direction !== 0) {
        resetRoomSliderInterval();
    }
}

function startRoomSliderAutoPlay() {
    const track = document.getElementById('roomSlider');
    if (!track) return;
    roomSliderInterval = setInterval(() => {
        moveRoomSlider(1);
    }, 6000); // 6 giây đổi ảnh phòng một lần
}

function resetRoomSliderInterval() {
    clearInterval(roomSliderInterval);
    startRoomSliderAutoPlay();
}

// Kích hoạt chạy tự động cùng lúc load trang
document.addEventListener('DOMContentLoaded', () => {
    startRoomSliderAutoPlay();
});

// Quick View Slider Script (Tours)
let currentQuickViewSlideIndex = 0;

function openQuickView(btn) {
    const hiddenDiv = btn.nextElementSibling;
    const spans = hiddenDiv.querySelectorAll('span');
    const imageUrls = Array.from(spans).map(s => s.textContent.trim());
    
    const sliderImages = document.getElementById('quickViewSliderImages');
    sliderImages.innerHTML = '';
    
    if(imageUrls.length === 0) return;

    imageUrls.forEach(url => {
        const img = document.createElement('img');
        img.src = url;
        sliderImages.appendChild(img);
    });
    
    currentQuickViewSlideIndex = 0;
    updateQuickViewPosition();
    
    document.getElementById('quickViewModal').style.display = 'block';
}

function closeQuickView() {
    document.getElementById('quickViewModal').style.display = 'none';
}

function moveQuickViewSlide(n) {
    const sliderImages = document.getElementById('quickViewSliderImages');
    if (!sliderImages) return;
    const totalSlides = sliderImages.children.length;
    if(totalSlides === 0) return;
    
    currentQuickViewSlideIndex += n;
    
    if (currentQuickViewSlideIndex >= totalSlides) {
        currentQuickViewSlideIndex = 0;
    }
    if (currentQuickViewSlideIndex < 0) {
        currentQuickViewSlideIndex = totalSlides - 1;
    }
    
    updateQuickViewPosition();
}

function updateQuickViewPosition() {
    const sliderImages = document.getElementById('quickViewSliderImages');
    if (sliderImages) {
        sliderImages.style.transform = `translateX(-${currentQuickViewSlideIndex * 100}%)`;
    }
}

window.addEventListener('click', function(event) {
    const modal = document.getElementById('quickViewModal');
    if (event.target == modal) {
        closeQuickView();
    }
});
