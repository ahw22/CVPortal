/*
 * Der einzige fetch()-Aufruf der Anwendung (F07).
 *
 * Er geht direkt vom Browser (Origin localhost:8081) an das Backend (localhost:8080).
 * Weil ein PUT mit Authorization-Header kein "simple request" ist, schickt der Browser
 * vorher einen OPTIONS-Preflight - genau der ist im Netzwerk-Tab sichtbar.
 *
 * Kein CSRF-Token: das Backend ist stateless und hat CSRF abgeschaltet. Der CSRF-Schutz
 * des Frontends gilt nur für die eigenen Formulare.
 */
(function () {
    const toggle = document.getElementById('visibility-toggle');
    if (!toggle) {
        return;
    }

    const status = document.getElementById('visibility-status');
    const url = toggle.dataset.url;
    const jwt = toggle.dataset.jwt;

    function melde(text, klasse) {
        status.textContent = text;
        status.className = 'mono small ' + klasse;
    }

    toggle.addEventListener('change', async function () {
        toggle.disabled = true;
        melde('wird gesendet …', 'text-body-secondary');

        try {
            const antwort = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + jwt
                },
                body: JSON.stringify({ publicVisible: toggle.checked })
            });

            const zeit = new Date().toLocaleTimeString('de-AT');

            if (antwort.ok) {
                melde('HTTP ' + antwort.status + ' · ' + zeit, 'text-success');   // statt status-ok
            } else {
                melde('HTTP ' + antwort.status + ' · ' + zeit, 'text-danger');    // statt status-fehler
                toggle.checked = !toggle.checked;
            }
        } catch (fehler) {
            melde('Netzwerkfehler · ' + fehler.message, 'status-fehler');
            toggle.checked = !toggle.checked;
        } finally {
            toggle.disabled = false;
        }
    });
})();