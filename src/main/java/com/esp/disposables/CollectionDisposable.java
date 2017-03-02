package com.esp.disposables;

import java.util.ArrayList;
import java.util.Arrays;

public class CollectionDisposable implements Disposable {
    private boolean _isDisposed = false;

    private ArrayList<Disposable> _disposables;

    public CollectionDisposable(Disposable ... disposables) {
        _disposables = new ArrayList<>(Arrays.asList(disposables));
    }

    public CollectionDisposable() {
        _disposables =  new ArrayList<>();
    }

    public boolean isDisposed() {
        return _isDisposed;
    }

    public void add(Disposable disposable) {
        if (_isDisposed) {
            disposable.dispose();
            return;
        }
        _disposables.add(disposable);
    }

    public void dispose() {
        if (_isDisposed) return;
        _isDisposed = true;
        for (Disposable disposable : _disposables) {
            disposable.dispose();
        }
    }
}
