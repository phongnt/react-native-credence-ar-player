import { 
    UIManager
} from 'react-native';

const create = (uiview: any) => {
  UIManager.dispatchViewManagerCommand(
    uiview,
    // @ts-ignore
    'create',
    [uiview]
  );
};

const destroy = (uiview: any) => {
  UIManager.dispatchViewManagerCommand(
    uiview,
    // @ts-ignore
    'destroy',
    [uiview]
  );
};

const addProduct = (uiview: any, productId: string, model: any, objectId: string) => {
  UIManager.dispatchViewManagerCommand(
    uiview,
    // @ts-ignore
    'add',
    [productId, model, objectId]
  );
};

const removeProduct = (uiview: any, objectId: string) => {
  UIManager.dispatchViewManagerCommand(
    uiview,
    // @ts-ignore
    'remove',
    [objectId]
  );
};

const clearProducts = (uiview: any) => {
  UIManager.dispatchViewManagerCommand(
    uiview,
    // @ts-ignore
    'clear',
    []
  );
};

const selectVariant = (uiview: any, productId: string, variant: any) => {
  UIManager.dispatchViewManagerCommand(
    uiview,
    // @ts-ignore
    'select',
    [productId, variant.id, variant.texture]
  )
};

export default {
    create,
    destroy,
    addProduct,
    removeProduct,
    clearProducts,
    selectVariant
};
