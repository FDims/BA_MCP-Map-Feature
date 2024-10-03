import MapComponent from "./MapComponent";


function App() {
    return (
        <div className="App">
            <MapComponent lat={51.5074} lon={-0.1278} minZoom={15} maxZoom={16} />
        </div>
    );
}

export default App;