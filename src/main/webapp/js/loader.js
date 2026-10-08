
(function() {
    // 1. Inject CSS Styles
    const style = document.createElement('style');
    style.innerHTML = `
        @keyframes logo-spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .animate-logo-spin { animation: logo-spin 2.5s linear infinite; } 
        .loading-overlay {
            position: fixed; inset: 0; background: white;
            display: flex; flex-direction: column; align-items: center;
            justify-content: center; z-index: 9999; transition: opacity 0.5s ease;
        }
        
        .loader-logo-container {
            width: 80px;  
            height: 80px; 
            border-radius: 50%; 
            overflow: hidden; 
            display: flex;
            align-items: center;
            justify-content: center;
            background: white; 
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
        }
        
        .loader-logo-img {
            max-width: 90%; 
            max-height: 90%;
            object-fit: contain; 
        }
    `;
    document.head.appendChild(style);

    // 2. Create and Inject HTML Structure
    const loaderDiv = document.createElement('div');
    loaderDiv.id = 'loader';
    loaderDiv.className = 'loading-overlay';
    
    loaderDiv.innerHTML = `
        <div class="loader-logo-container animate-logo-spin">
            <img src="images/logo2.jpg" class="loader-logo-img" alt="Loading...">
        </div>
        <p style="margin-top: 20px; font-family: sans-serif; color: #4b5563; font-weight: bold; font-size: 14px;">Easy2Drive טוען...</p>
    `;
    
    // Ensure injection occurs as soon as the body is available
    if (document.body) {
        document.body.prepend(loaderDiv);
    } else {
        document.addEventListener('DOMContentLoaded', () => document.body.prepend(loaderDiv));
    }

	/**
	 * Hides the loader with a fade-out effect.
	 */
    window.hideLoader = function() {
        const loader = document.getElementById('loader');
        if (loader) {
            loader.style.opacity = '0';
            setTimeout(() => loader.style.display = 'none', 500);
        }
    };

    // Auto-dismiss when the entire window (including images) is loaded
    window.addEventListener('load', window.hideLoader);
})();