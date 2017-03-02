package com.esp.disposables;

public abstract class DisposableBase implements Disposable {
    CollectionDisposable _disposables = new CollectionDisposable();

    public boolean isDisposed() {
        return _disposables.isDisposed();
    }

    public void addDisposable(Disposable disposable) {
        _disposables.add(disposable);
    }

    @Override
    public void dispose() {
        _disposables.dispose();
    }
}
