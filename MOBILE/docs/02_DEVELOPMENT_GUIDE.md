# 02 — Development Guide (Recipes)

Step-by-step recipes. Every example mirrors the existing **Auth** feature — use it as a living
template. Read [01_ARCHITECTURE.md](01_ARCHITECTURE.md) first.

---

## Recipe A — Add a New Feature (end-to-end)

Example: a **`product`** feature (toy list). Replace `product`/`Product` with your feature name.
Build from the data layer outward.

### 1. Model — `data/models/product.dart`
```dart
import 'package:equatable/equatable.dart';

class Product extends Equatable {
  const Product({required this.id, required this.name, required this.price});

  factory Product.fromJson(Map<String, dynamic> json) => Product(
        id: json['id']?.toString() ?? '',
        name: json['name']?.toString() ?? '',
        price: (json['price'] as num?)?.toDouble() ?? 0,
      );

  final String id;
  final String name;
  final double price;

  Map<String, dynamic> toJson() => {'id': id, 'name': name, 'price': price};

  @override
  List<Object?> get props => [id, name, price];
}
```

### 2. Service — `data/services/product_api_service.dart`
```dart
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../config/api_config.dart';
import '../../core/exceptions/app_exception.dart';
import '../../core/network/api_client.dart';
import '../models/product.dart';

final productApiServiceProvider = Provider<ProductApiService>((ref) {
  return ProductApiService(ref.watch(apiClientProvider));
});

class ProductApiService {
  const ProductApiService(this._dio);

  final Dio _dio;

  Future<List<Product>> fetchProducts() async {
    try {
      final res = await _dio.get(ApiConfig.products);
      final list = res.data['data'] as List;
      return list
          .map((e) => Product.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      final error = e.error;
      if (error is AppException) throw error;
      throw ServerException(e.message ?? 'Failed to load products');
    }
  }
}
```

### 3. Repository — `data/repositories/product_repository.dart`
```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/product.dart';
import '../services/product_api_service.dart';

final productRepositoryProvider = Provider<ProductRepository>((ref) {
  return ProductRepository(ref.watch(productApiServiceProvider));
});

class ProductRepository {
  const ProductRepository(this._api);

  final ProductApiService _api;

  Future<List<Product>> getProducts() => _api.fetchProducts();
}
```

> A use case is NOT needed here — the repository call is a thin pass-through. Add a
> `domain/use_cases/` class only when logic is shared or complex (see Recipe E).

### 4. View Model — `ui/product/view_model/product_list_view_model.dart`
For a read-only list, an `AsyncNotifier` gives you loading/error/data for free:
```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/models/product.dart';
import '../../../data/repositories/product_repository.dart';

class ProductListViewModel extends AsyncNotifier<List<Product>> {
  @override
  Future<List<Product>> build() {
    return ref.watch(productRepositoryProvider).getProducts();
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(
      () => ref.read(productRepositoryProvider).getProducts(),
    );
  }
}

final productListViewModelProvider =
    AsyncNotifierProvider<ProductListViewModel, List<Product>>(
  ProductListViewModel.new,
);
```

### 5. Screen — `ui/product/widgets/product_list_screen.dart`
See **Recipe B** (the three mandatory states).

### 6. Route — `routing/routes.dart` + `routing/router.dart`
```dart
// routes.dart
static const String products = '/products';

// router.dart
GoRoute(
  path: Routes.products,
  name: 'products',
  builder: (_, _) => const ProductListScreen(),
),
```

### 7. Endpoint — `config/api_config.dart`
```dart
static const String products = '/products';
```

✅ **Checklist:** model → service → repository → (optional use case) → view model → screen →
route → endpoint constant → test.

---

## Recipe B — A data-loading screen (handle 3 states)

Every screen that fetches data MUST handle loading / error / empty. An `AsyncNotifier` plus
`.when()` keeps this tidy:

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/exceptions/app_exception.dart';
import '../../core/themes/spacing.dart';
import '../../core/widgets/app_card.dart';
import '../../core/widgets/app_empty_state.dart';
import '../../core/widgets/app_error_view.dart';
import '../../core/widgets/app_loading.dart';
import '../../core/widgets/screen_wrapper.dart';
import '../view_model/product_list_view_model.dart';

class ProductListScreen extends ConsumerWidget {
  const ProductListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final products = ref.watch(productListViewModelProvider);

    return ScreenWrapper(
      appBar: AppBar(title: const Text('Produk')),
      child: products.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorView(
          message: e is AppException ? e.message : 'Terjadi kesalahan',
          onRetry: () =>
              ref.read(productListViewModelProvider.notifier).refresh(),
        ),
        data: (list) {
          if (list.isEmpty) {
            return const AppEmptyState(
              icon: Icons.inventory_2_outlined,
              title: 'Belum ada produk',
            );
          }
          return ListView.separated(
            itemCount: list.length,
            separatorBuilder: (_, _) => const SizedBox(height: AppSpacing.sm),
            itemBuilder: (_, i) => AppCard(child: Text(list[i].name)),
          );
        },
      ),
    );
  }
}
```

> Check the exact widget signatures (`AppLoading`, `AppErrorView`, `AppEmptyState`, `AppCard`)
> in [03_DESIGN_SYSTEM.md](03_DESIGN_SYSTEM.md) before using them.

---

## Recipe C — Add an API call to an existing feature

1. Add the endpoint to `config/api_config.dart`.
2. Add the method to the feature's **service**, using the injected `apiClientProvider` dio.
   NEVER create a new `Dio` instance.
3. Surface it through the **repository** (let `AppException` propagate, or catch for a fallback).
4. Call it from the **view model** (or a use case), update state.

---

## Recipe D — Mutable state / form submission

Use a `Notifier` with an immutable state class (the `AuthViewModel` pattern):

```dart
class ProductFormState {
  const ProductFormState({this.isLoading = false, this.error, this.saved = false});

  final bool isLoading;
  final String? error;
  final bool saved;

  ProductFormState copyWith({
    bool? isLoading,
    String? error,
    bool clearError = false,
    bool? saved,
  }) {
    return ProductFormState(
      isLoading: isLoading ?? this.isLoading,
      error: clearError ? null : error ?? this.error,
      saved: saved ?? this.saved,
    );
  }
}

class ProductFormViewModel extends Notifier<ProductFormState> {
  @override
  ProductFormState build() => const ProductFormState();

  Future<void> submit(/* params */) async {
    state = state.copyWith(isLoading: true, clearError: true);
    try {
      await ref.read(productRepositoryProvider).create(/* ... */);
      state = state.copyWith(isLoading: false, saved: true);
    } on AppException catch (e) {
      state = state.copyWith(isLoading: false, error: e.message);
    }
  }
}

final productFormViewModelProvider =
    NotifierProvider<ProductFormViewModel, ProductFormState>(
  ProductFormViewModel.new,
);
```

In the screen, surface errors with `ref.listen` + `AppSnackbar.showError` (see
[`login_screen.dart`](../lib/ui/auth/widgets/login_screen.dart)).

---

## Recipe E — When (and how) to add a Use Case

Add a use case **only** when business logic is shared across view models or complex enough to
test in isolation. Otherwise call the repository directly from the view model.

`domain/use_cases/rank_products_use_case.dart`:
```dart
import '../../data/models/product.dart';

/// Pure business logic — no Flutter, no dio. Easy to unit-test.
class RankProductsUseCase {
  const RankProductsUseCase();

  List<Product> call(List<Product> products) {
    final sorted = [...products]..sort((a, b) => b.price.compareTo(a.price));
    return sorted;
  }
}
```

Expose it via a provider and call it from the view model. This is where SPK scoring logic
(SAW/WP/TOPSIS, etc.) will live once the domain is defined.

---

## Connecting the real backend (remove the mock)

The mock auth lives in [`auth_view_model.dart`](../lib/ui/auth/view_model/auth_view_model.dart).
To go live:

1. Delete the `MOCK AUTH` block in `AuthViewModel.login()` (the real path is already below it).
2. Set `ApiConfig.baseUrl` to the real backend URL.
3. Verify the login/profile JSON shapes match `User.fromJson` and `AuthApiService`.
