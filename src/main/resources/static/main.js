
const http = new XMLHttpRequest();

const tbody = document.querySelector("tbody#appointments");
const template = document.querySelector("#appointmentRow");
const titleInput = document.querySelector("#title");
const orderInput = document.querySelector("#order");

function httpSend(method, url, body, callback) {
    http.open(method, url);
    http.setRequestHeader('Content-type', 'application/json');
    http.send(JSON.stringify(body));
    http.onload = callback;
}

function httpGet(url, body, callback) {
    httpSend('GET', url, body, callback);
}

function httpPost(url, body, callback) {
    httpSend('POST', url, body, callback);
}

function addTask(task) {
    const clone = template.content.cloneNode(true);
    const td = clone.querySelectorAll("td");
    const input = clone.querySelectorAll("input");
    td[0].textContent = task.title;
    td[1].textContent = task.order;
    input.checked = task.completed;
    tbody.appendChild(clone);
}

function add() {
    const body = {
        title: titleInput.value,
        order: orderInput.valueAsNumber
    };

    httpPost('/appointments', body, () => {
        titleInput.value = "";
        orderInput.value = 0;

        addTask(JSON.parse(http.responseText));
    });
}

function main() {

    httpGet('/appointments', null, () => {
        for (const tr of tbody.children)
            tr.remove();

        const response = JSON.parse(http.responseText);
        for (const task of response)
            addTask(task);

        console.log(http.responseText);
    });
}

document.body.onload = main;
